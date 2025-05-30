class ValidatorClient {
  constructor() {
    this.stompClient = null;
    this.validatorId = null;
    this.publicKey = null;
    this.callbacks = new Map();
    this.isConnecting = false;
    this.isAuthenticating = false;
    this.provider = null;
    this.signMessage = null;
    this.isServerVerified = false;
  }
  async initializeProvider() {
    if (!this.provider) {
      this.provider = window.solana;
      if (!this.provider?.isPhantom) return false;

      // Set up provider listeners
      this.provider.on("connect", () => {
        console.log("Wallet connected:", this.provider.publicKey.toString());
      });

      this.provider.on("disconnect", () => {
        this.handleDisconnect();
      });
    }
    return true;
  }

  async connectSolana() {
    if (this.isConnecting || !(await this.initializeProvider())) return;
    this.isConnecting = true;
    try {
      this.provider = window.solana;
      // if (!this.provider || !this.provider.isPhantom) {
      //   throw new Error("Phantom Wallet required");
      // }
      showMessage("info", "Connecting to Phantom Wallet...", 0);

      const connection = await this.provider.connect({ onlyIfTrusted: false });
      if (!this.provider.isConnected) throw new Error("Connection canceled");

      this.publicKey = this.provider.publicKey.toString();
      document.getElementById("authenticateBtn").disabled = false;
      showSuccess("Wallet connected!", 2000);
    } catch (error) {
      showError(error.message);
    } finally {
      this.isConnecting = false;
    }
  }

  async connectWebSocket() {
    // if (!window.solana?.isPhantom) {
    //   throw new Error("Phantom Wallet not detected");
    // }

    // // Connect to wallet
    // await window.solana.connect();
    // this.publicKey = window.solana.publicKey.toString();

    // Setup STOMP connection
    if (this.stompClient?.connected) return;
    showMessage("info", "Establishing secure connection...", 0);
    try {
      const socket = new SockJS("/ws");
      this.stompClient = Stomp.over(socket);

      await new Promise((resolve, reject) => {
        this.stompClient.connect({}, resolve, reject);
      });

      this.handleConnectionSuccess();
      showSuccess("Connection established!", 2000);
    } catch (error) {
      this.handleConnectionError(error);
      showError(`Connection failed: ${error}`);
      throw error;
    }

    // const socket = new SockJS("/ws");
    // this.stompClient = Stomp.over(socket);

    // this.stompClient.connect(
    //   {},
    //   () => {
    //     this.handleConnectionSuccess();
    //   },
    //   (error) => {
    //     this.handleConnectionError(error);
    //   }
    // );
  }
  disconnect() {
    if (this.stompClient?.connected) {
      this.stompClient.disconnect();
    }
    this.callbacks.clear();
    this.validatorId = null;
  }
  handleDisconnect() {
    this.publicKey = null;
    this.validatorId = null;
    this.disconnect();
    showError("Wallet disconnected", 5000);
    document.getElementById("authenticateBtn").disabled = true;
  }
  handleConnectionSuccess() {
    // Subscribe to private queues
    this.stompClient.subscribe("/user/queue/signup", (response) => {
      const data = JSON.parse(response.body);
      this.handleSignupResponse(data);
    });

    this.stompClient.subscribe("/user/queue/validate", (response) => {
      const data = JSON.parse(response.body);
      this.handleValidationRequest(data);
    });
    this.stompClient.subscribe("/user/queue/url-updates", (response) => {
      const data = JSON.parse(response.body);
      this.handleUrlUpdate(data);
    });

    this.stompClient.subscribe("/user/queue/balance-updates", (response) => {
      const data = JSON.parse(response.body);
      this.updateBalance(data);
    });
    this.stompClient.subscribe("/user/queue/errors", (response) => {
      const error = JSON.parse(response.body);
      showError(error.message);
    });

    // Initiate signup process
    this.sendSignupRequest();
  }

  async sendSignupRequest() {
    if (!this.provider?.isConnected || this.isAuthenticating) return;

    this.isAuthenticating = true;
    const callbackId = crypto.randomUUID();
    const message = `Signed message for ${callbackId}, ${this.publicKey}`;
    const encodedMessage = new TextEncoder().encode(message);

    try {
      const { signature } = await this.provider.signMessage(encodedMessage);
      const base64Signature = btoa(
        String.fromCharCode(...new Uint8Array(signature))
      );

      this.stompClient.send(
        "/app/validator/login",
        {},
        JSON.stringify({
          publicKey: this.publicKey,
          signature: base64Signature,
          callbackId: callbackId,
          ip: window.location.hostname,
        })
      );

      this.callbacks.set(callbackId, (response) => {
        this.validatorId = response.validatorId;
        showSuccess("Validator registered successfully!");
        // window.location.href = "/validator/dashboard";
        this.showDashboard();
      });
    } catch (error) {
      showError("Signing failed: " + error.message);
    }
  }

  async handleValidationRequest(request) {
    this.updateUrlList({
      websiteId: request.websiteId,
      url: request.url,
      status: "CHECKING",
    });
    const { callbackId, url } = request;

    const message = `Replying to ${request.callbackId}`;
    const encodedMessage = new TextEncoder().encode(message);
    const { signature } = await this.provider.signMessage(encodedMessage);
    const responseSignature = btoa(
      String.fromCharCode(...new Uint8Array(signature))
    );

    try {
      const startTime = Date.now();
      const response = await fetch(request.url);
      const latency = Date.now() - startTime;

      this.stompClient.send(
        "/app/validate",
        {},
        JSON.stringify({
          callbackId: request.callbackId,
          status: response.ok ? "SUCCESS" : "FAILURE",
          latency: latency,
          signature: responseSignature,
          websiteId: request.websiteId,
        })
      );
      this.updateUrlList({
        websiteId: request.websiteId,
        url: request.url,
        status: response.ok ? "SUCCESS" : "FAILURE",
      });
    } catch (error) {
      console.log("Validation error:", error);
      try {
        console.log("Attempting to fetch with CORS proxy...");
        const response = await fetch(
          `http://localhost:8732/check?url=${encodeURIComponent(url)}`
        );
        const result = await response.json();
        console.log("CORS proxy response:", result);
        this.stompClient.send(
          "/app/validate",
          {},
          JSON.stringify({
            callbackId: request.callbackId,
            status: result.status === 200 ? "SUCCESS" : "FAILURE",
            latency: result.latency,
            signature: responseSignature,
            websiteId: request.websiteId,
          })
        );
        this.updateUrlList({
          websiteId: request.websiteId,
          url: request.url,
          status: response.ok ? "SUCCESS" : "FAILURE",
        });
        console.log("CORS proxy response:", result);
      } catch (error) {
        this.stompClient.send(
          "/app/validate",
          {},
          JSON.stringify({
            callbackId: request.callbackId,
            status: "FAILURE",
            latency: 1000,
            signature: responseSignature,
            websiteId: request.websiteId,
          })
        );
        this.updateUrlList({
          websiteId: request.websiteId,
          url: request.url,
          status: "FAILURE",
        });
      }
    }
  }

  /*
 catch (error) {
      // console.log("Validation111 error:", error);
      // if (CORS_ERROR_CODES.includes(error.code)) {
      //   JSON.stringify({
      //     callbackId: request.callbackId,
      //     status: "SUCCESS",
      //     latency: 144,
      //     signature: responseSignature,
      //     websiteId: request.websiteId,
      //   });
      //   this.logCorsIssue(error);
      //   return;
      // }

      try {
        const startTime = Date.now();
        const proxyUrl = `https://cors-anywhere.herokuapp.com/get?url=${encodeURIComponent(
          request.url
        )}`;
        // const response = await fetch(proxyUrl, {
        //   headers: {
        //     "X-Requested-With": "XMLHttpRequest",
        //     Origin: window.location.origin,
        //   },
        // });

        const response = await this.fetchWithCORS(request.url);
        const latency = Date.now() - startTime;

        this.stompClient.send(
          "/app/validate",
          {},
          JSON.stringify({
            callbackId: request.callbackId,
            status: response.ok ? "SUCCESS" : "FAILURE",
            latency: latency,
            signature: responseSignature,
            websiteId: request.websiteId,
          })
        );
      }
          async fetchWithCORS(url) {
    var cors_api_url = "https://cors-anywhere.herokuapp.com/";
    var x = new XMLHttpRequest();
    x.open("GET", cors_api_url + url); // it will send request to the server?

    x.onload = x.onerror = function () {
      //
      if (x.status >= 200 && x.status < 300) {
        console.log("CORS request successful:", x.responseText);
      } else {
        console.error("CORS request failed:", x.statusText);
        throw new Error("CORS request failed");
      }
    };

    return new Promise((resolve, reject) => {
      x.send();
    });
  }

    */

  handleSignupResponse(response) {
    const callback = this.callbacks.get(response.callbackId);
    if (callback) {
      callback(response);
      this.callbacks.delete(response.callbackId);
    }
  }

  handleConnectionError(error) {
    showError("WebSocket connection error: " + error.toString());
    setTimeout(() => this.connect(), 5000); // Reconnect after 5 seconds
  }
  updateUrlList(urlInfo) {
    if (!this.isDashboardVisible) return;

    const urlList = document.getElementById("activeUrls");
    const existingItem = Array.from(urlList.children).find(
      (item) => item.dataset.websiteId === String(urlInfo.websiteId)
    );
    let earnedCredits =
      parseInt(document.getElementById("earnedCredits").textContent) || 0;
    let urlchecked =
      parseInt(document.getElementById("pendingChecks").textContent) || 0;
    if (urlInfo.status != "CHECKING") {
      earnedCredits += 100;
      urlchecked += 1;
    }
    const urlItem = document.createElement("div");
    urlItem.className = `url-item ${urlInfo.status.toLowerCase()}`;
    urlItem.dataset.websiteId = urlInfo.websiteId;
    urlItem.innerHTML = `
      <span>${urlInfo.url}</span>
      <span class="status">${urlInfo.status}</span>
      <span>${new Date().toLocaleTimeString()}</span>
    `;

    if (existingItem) {
      urlList.replaceChild(urlItem, existingItem);
    } else {
      urlList.appendChild(urlItem);
    }

    // Update pending checks count

    document.getElementById("pendingChecks").textContent = urlchecked;
    document.getElementById("earnedCredits").textContent = earnedCredits;
  }
  // handleUrlUpdate(urlInfo) {
  //   if (window.location.pathname === "/validator/dashboard") {
  //     const event = new CustomEvent("urlUpdate", { detail: urlInfo });
  //     document.dispatchEvent(event);
  //   }
  // }

  // handleBalanceUpdate(balanceInfo) {
  //   if (window.location.pathname === "/validator/dashboard") {
  //     const event = new CustomEvent("balanceUpdate", {
  //       detail: {
  //         pendingBalance: balanceInfo.pendingBalance,
  //         totalEarned: balanceInfo.totalEarned,
  //       },
  //     });
  //     document.dispatchEvent(event);
  //   }
  // }

  updateBalance(balanceInfo) {
    if (!this.isDashboardVisible) return;
    console.log("bslanceeeeeeee");
    console.log(balanceInfo.pendingBalance);
    document.getElementById(
      "balance"
    ).textContent = `Balance: ${balanceInfo.pendingBalance} DPIN`;
  }
  showDashboard() {
    this.isDashboardVisible = true;
    sessionStorage.setItem("validatorId", this.validatorId);
    sessionStorage.setItem("publicKey", this.publicKey);

    document.getElementById("authContainer").classList.add("hidden");
    document.getElementById("dashboardContainer").classList.remove("hidden");

    // Initialize dashboard
    // document.getElementById(
    //   "walletAddress"
    // ).textContent = `${this.publicKey.substring(0, 6)}...${this.publicKey.slice(
    //   -4
    // )}`;

    this.fetchBalance();
    this.setupDashboardListeners();
  }
  initDashboard() {
    // Populate dashboard data
    // document.getElementById(
    //   "walletAddress"
    // ).textContent = `${this.publicKey.substring(0, 6)}...${this.publicKey.slice(
    //   -4
    // )}`;

    // Load initial balance
    this.fetchBalance();

    // Setup dashboard WebSocket subscriptions
    // this.stompClient.subscribe("/user/queue/dashboard", (message) => {
    //   this.handleDashboardUpdate(JSON.parse(message.body));
    // });
  }
  async fetchBalance() {
    try {
      this.stompClient.send(
        "/app/validator/balance",
        {},
        JSON.stringify({
          publicKey: this.publicKey,
        })
      );
      document.getElementById(
        "balance"
      ).textContent = `Balance: ${data.pendingBalance} DPIN`;
    } catch (error) {
      console.error("Balance fetch failed:", error);
    }
  }

  // handleDashboardUpdate(update) {
  //   // Handle real-time updates
  //   switch (update.type) {
  //     case "BALANCE_UPDATE":
  //       document.getElementById(
  //         "balance"
  //       ).textContent = `Balance: ${update.pendingBalance} DPIN`;
  //       break;
  //     case "URL_UPDATE":
  //       this.updateUrlList(update.urlInfo);
  //       break;
  //   }
  // }

  setupDashboardListeners() {
    document.getElementById("checkBalanceBtn").addEventListener("click", () => {
      console.log("checking balnce");
      this.fetchBalance();
      showMessage("info", "Checking balance...", 2000);
    });

    // document
    //   .getElementById("verifyServerBtn")
    //   .addEventListener("click", async () => {
    //     try {
    //       showMessage("info", "Verifying server...", 0);
    //       const response = await fetch("/validator/verify-server", {
    //         method: "POST",
    //       });

    //       if (response.ok) {
    //         this.isServerVerified = true;
    //         showSuccess("Server verified successfully!", 2000);
    //       } else {
    //         throw new Error("Server verification failed");
    //       }
    //     } catch (error) {
    //       showError(error.message);
    //     }
    //   });
  }
}

// UI Functions
// function showError(message) {
//   const errorDiv = document.createElement("div");
//   errorDiv.className = "alert alert-error";
//   errorDiv.innerHTML = `
//         <i class="fas fa-exclamation-circle"></i>
//         <span>${message}</span>
//     `;
//   const errorDiv2 = document.getElementsByClassName("wallet-connection-error");
//   errorDiv2.innerHTML = errorDiv;
// }

// function showSuccess(message) {
//   const successDiv = document.createElement("div");
//   successDiv.className = "alert alert-success";
//   successDiv.innerHTML = `
//         <i class="fas fa-check-circle"></i>
//         <span>${message}</span>
//     `;
//   const successDiv2 = document.getElementsByClassName("proccess-message");
//   successDiv2.innerHTML = successDiv;
// }
// Replace existing showError/showSuccess with:
const messageContainer = document.getElementById("messageContainer");

function showMessage(type, text, duration = 3000) {
  messageContainer.innerHTML = `
    <div class="alert alert-${type}">
      <i class="fas fa-${
        type === "error" ? "exclamation" : "check"
      }-circle"></i>
      <span>${text}</span>
    </div>
  `;
  if (duration) setTimeout(() => (messageContainer.innerHTML = ""), duration);
}

function showError(text, duration) {
  showMessage("error", text, duration);
}

function showSuccess(text, duration) {
  showMessage("success", text, duration);
}
// Initialize Validator Client
document.addEventListener("DOMContentLoaded", () => {
  const validator = new ValidatorClient();
  let authInProgress = false;

  document
    .getElementById("connectWallet")
    .addEventListener("click", async () => {
      if (authInProgress) return;
      authInProgress = true;
      await validator.connectSolana();
      authInProgress = false;
    });

  document
    .getElementById("authenticateBtn")
    .addEventListener("click", async () => {
      if (!validator.publicKey || authInProgress) return;

      try {
        authInProgress = true;
        showMessage("info", "Starting authentication...", 0);

        await validator.connectWebSocket();
        // await validator.sendSignupRequest();

        showSuccess("Authentication complete!", 2000);
        // Redirect or enable further functionality
      } catch (error) {
        showError(`Authentication failed: ${error.message}`);
      } finally {
        authInProgress = false;
      }
    });

  document.getElementById("disconnectBtn").addEventListener("click", () => {
    if (validator.stompClient?.connected) {
      validator.stompClient.disconnect();
    }
    window.location.href = "/login";
  });
});
//   document
//     .getElementById("connectWallet")
//     .addEventListener("click", async () => {
//       try {
//         console.log("Connect Wallet Button clicked");
//         await validator.connectSolana();
//         console.log("Wallet connected");
//         showSuccess("Wallet connected! Authentication in progress...");
//       } catch (error) {
//         showError(error.message);
//       }
//     });
//   document
//     .getElementById("authenticateBtn")
//     .addEventListener("click", async () => {
//       try {
//         await validator.connect();
//         showSuccess("Validator authentication in progress...");
//       } catch (error) {
//         showError(error.message);
//       }
//     });
// });

// // validator-client.js
// class ValidatorClient {
//   constructor() {
//     this.ws = null;
//     this.validatorId = null;
//     this.callbacks = new Map();
//     this.publicKey = null;
//   }

//   async connect() {
//     // Connect to Solana wallet
//     if (!window.solana?.isPhantom) {
//       throw new Error("Phantom Wallet not detected");
//     }

//     await window.solana.connect();
//     this.publicKey = window.solana.publicKey.toString();

//     // Setup WebSocket connection
//     // this.ws = new WebSocket("ws://localhost:8080/ws");

//     this.ws.onmessage = async (event) => {
//       const data = JSON.parse(event.data);
//       if (data.type === "signup") {
//         const callback = this.callbacks.get(data.data.callbackId);
//         if (callback) {
//           callback(data.data);
//           this.callbacks.delete(data.data.callbackId);
//         }
//       } else if (data.type === "validate") {
//         await this.handleValidation(data.data);
//       }
//     };

//     this.ws.onopen = async () => {
//       const callbackId = crypto.randomUUID();

//       // Create and sign message
//       const message = `Signed message for ${callbackId}, ${this.publicKey}`;
//       const encodedMessage = new TextEncoder().encode(message);
//       const { signature: signedMessage } = await window.solana.signMessage(
//         encodedMessage
//       );

//       // Convert signature to base64
//       const base64Signature = btoa(
//         String.fromCharCode(...new Uint8Array(signedMessage))
//       );

//       this.callbacks.set(callbackId, (response) => {
//         this.validatorId = response.validatorId;
//       });

//       this.ws.send(
//         JSON.stringify({
//           type: "signup",
//           data: {
//             callbackId,
//             ip: window.location.hostname,
//             publicKey: this.publicKey,
//             signedMessage: base64Signature,
//           },
//         })
//       );
//     };
//   }

//   async handleValidation(request) {
//     const startTime = Date.now();
//     const message = `Replying to ${request.callbackId}`;

//     try {
//       // Sign response message
//       const encodedMessage = new TextEncoder().encode(message);
//       const { signature } = await window.solana.signMessage(encodedMessage);
//       const responseSignature = btoa(
//         String.fromCharCode(...new Uint8Array(signature))
//       );

//       // Validate URL
//       const response = await fetch(request.url);
//       const latency = Date.now() - startTime;

//       this.ws.send(
//         JSON.stringify({
//           type: "validate",
//           data: {
//             callbackId: request.callbackId,
//             status: response.ok ? "Good" : "Bad",
//             latency,
//             websiteId: request.websiteId,
//             validatorId: this.validatorId,
//             signature: responseSignature,
//           },
//         })
//       );
//     } catch (error) {
//       console.error("Validation failed:", error);
//       this.ws.send(
//         JSON.stringify({
//           type: "validate",
//           data: {
//             callbackId: request.callbackId,
//             status: "Bad",
//             latency: 1000,
//             websiteId: request.websiteId,
//             validatorId: this.validatorId,
//             signature: responseSignature,
//           },
//         })
//       );
//     }
//   }
// }

// // Usage
// const validator = new ValidatorClient();

// // Connect button handler
// document.getElementById("connect-btn").addEventListener("click", async () => {
//   try {
//     await validator.connect();
//     console.log("Successfully connected as validator");
//   } catch (error) {
//     console.error("Validator connection failed:", error);
//   }
// });
