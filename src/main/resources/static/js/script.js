document.addEventListener("DOMContentLoaded", function () {
  // Smooth scrolling for anchor links
  document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
    anchor.addEventListener("click", function (e) {
      e.preventDefault();

      const target = document.querySelector(this.getAttribute("href"));
      if (target) {
        window.scrollTo({
          top: target.offsetTop - 80, // Account for header height
          behavior: "smooth",
        });
      }
    });
  });

  // Reveal animations for sections
  const observerOptions = {
    threshold: 0.1,
    rootMargin: "0px 0px -50px 0px",
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add("reveal-visible");
        observer.unobserve(entry.target);
      }
    });
  }, observerOptions);

  document
    .querySelectorAll(
      ".features-grid > *, .user-types-grid > *, .steps > *, .testimonials-slider > *"
    )
    .forEach((el) => {
      el.classList.add("reveal");
      observer.observe(el);
    });

  // Add reveal CSS if not in stylesheet
  if (!document.querySelector("style#reveal-styles")) {
    const style = document.createElement("style");
    style.id = "reveal-styles";
    style.textContent = `
            .reveal {
                opacity: 0;
                transform: translateY(30px);
                transition: opacity 0.6s ease, transform 0.6s ease;
            }
            .reveal-visible {
                opacity: 1;
                transform: translateY(0);
            }
        `;
    document.head.appendChild(style);
  }

  // Header scroll effect
  const header = document.querySelector("header");
  let lastScrollTop = 0;

  window.addEventListener("scroll", () => {
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;

    if (scrollTop > 100) {
      header.style.boxShadow = "var(--shadow-md)";
      header.style.background = "rgba(255, 255, 255, 0.95)";
    } else {
      header.style.boxShadow = "var(--shadow-sm)";
      header.style.background = "white";
    }

    lastScrollTop = scrollTop;
  });
});



// document.getElementById("connectWallet").addEventListener("click", async () => {
//   try {
//     const provider = window.solana;
//     if (!provider || !provider.isPhantom) {
//       showError("Phantom Wallet not detected!");
//       return;
//     }

//     // Connect to wallet
//     const connection = await provider.connect();

//     // Get required values
//     const publicKey = provider.publicKey.toString();
//     const message = `Validator Authentication - ${Date.now()}`;

//     // Sign message
//     const encodedMessage = new TextEncoder().encode(message);
//     const signedMessage = await provider.signMessage(encodedMessage);

//     // Update form fields
//     document.getElementById("publicKey").value = publicKey;
//     document.getElementById("message").value = message;
//     // document.getElementById("signedMessage").value = JSON.stringify(
//     //   Array.from(signedMessage.signature)
//     // );
//     const signatureBytes = new Uint8Array(signedMessage.signature);
//     const base64Signature = btoa(String.fromCharCode(...signatureBytes));
//     document.getElementById("signedMessage").value = base64Signature;

//     // Show authentication button
//     document.getElementById("validatorAuthForm").style.display = "block";
//     document.getElementById("connectWallet").disabled = true;
//     showSuccess("Wallet connected!");
//     // document.getElementById("authenticateBtn").disabled = false;
//   } catch (error) {
//     console.error("Wallet error:", error);
//     showError(error.message);
//   }
// });
// document.getElementById("connectWallet").addEventListener("click", async () => {
//   try {
//     const provider = window.solana;
//     if (!provider?.isPhantom) throw new Error("Phantom Wallet required");

//     await provider.connect();
//     publicKey = provider.publicKey.toString();
//     document.getElementById("authenticateBtn").disabled = false;
//     showSuccess("Wallet connected!");
//   } catch (error) {
//     showError(error.message);
//   }
// });

// document
//   .getElementById("validatorAuthBtn")
//   .addEventListener("click", async () => {
//     // After wallet connection
//     console.log("Validator Auth Button clicked");
//     const publicKey = document.getElementById("publicKey").value;

//     // const signedMessage = JSON.parse(document.getElementById("signedMessage").value);
//     // const signature = bs58.encode(Uint8Array.from(signedMessage));
//     // const signedMessage = document.getElementById("signedMessage").value;
//     // const signature = bs58.encode(signedMessage);
//     // const signature = JSON.parse(
//     //   document.getElementById("signedMessage").value
//     // );

//     const base64Signature = document.getElementById("signedMessage").value;

//     const message = document.getElementById("message").value;
//     if (stompClient && stompClient.connected) {
//       stompClient.disconnect();
//     }
//     stompClient = Stomp.over(new SockJS("/ws"));
//     stompClient.connect(
//       {},
//       () => {
//         // Handle response
//         stompClient.subscribe(`/user/queue/signup`, (response) => {
//           showSuccess("Authentication successful! Redirecting...");
//           console.log("" + response.body + "  " + response);
//           // console.log("header" + response. + "  " + response);

//           // setTimeout(() => (window.location.href = "..."), 2000);
//           // window.location.href = "/validator/dashboard";
//         });
//         stompClient.subscribe(`/user/queue/errors`, (error) => {
//           showError(JSON.parse(error.body).error);
//         });
//         // Send login request via WebSocket
//         stompClient.send(
//           "/app/validator/login",
//           {},
//           JSON.stringify({
//             publicKey: publicKey,
//             signature: base64Signature,
//             message: message,
//             callbackId: generateUUID(), // Client-generated
//           })
//         );
//       },
//       (error) => {
//         console.error("WebSocket error:", error);

//         showError("WebSocket connection failed! ${error.message}");
//       }
//     );
//   });

// Helper functions
