document.addEventListener('DOMContentLoaded', function() {
    // Password strength indicator
    const passwordInput = document.getElementById('password');
    if(passwordInput) {
        const strengthMeter = document.getElementById('strength-meter');
        
        passwordInput.addEventListener('input', function() {
            const password = this.value;
            let strength = 0;
            
            // Length check
            if(password.length >= 8) strength += 25;
            // Uppercase check
            if(/[A-Z]/.test(password)) strength += 25;
            // Number check
            if(/\d/.test(password)) strength += 25;
            // Special character check
            if(/[^A-Za-z0-9]/.test(password)) strength += 25;
            
            strengthMeter.style.width = strength + '%';
            strengthMeter.style.backgroundColor = 
                strength < 50 ? '#f72585' : 
                strength < 75 ? '#f8961e' : '#4cc9f0';
        });
    }
});