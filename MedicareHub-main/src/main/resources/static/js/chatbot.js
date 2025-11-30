// Chatbot script
document.addEventListener("DOMContentLoaded", () => {
    const chatButton = document.getElementById("chatbot-button");
    const chatPopup = document.getElementById("chatbot-popup");
    const closeChat = document.getElementById("close-chat");
    const sendButton = document.getElementById("send-message");
    const chatInput = document.getElementById("chat-input");
    const chatBody = document.getElementById("chat-body");
    chatButton.addEventListener("click", () => {
        chatPopup.style.display = "flex";
    });
    closeChat.addEventListener("click", () => {
        chatPopup.style.display = "none";
    });
    sendButton.addEventListener("click", () => {
        sendMessage();
    });
    chatInput.addEventListener("keypress", (event) => {
        if (event.key === "Enter") {
            sendMessage();
        }
    });
    function sendMessage() {
        const message = chatInput.value.trim();
        if (message === "") return;
        appendMessage("Bạn", message);
        sendMessageToN8n(message);
        chatInput.value = "";
    }
    function sendMessageToN8n(message) {
        fetch("https://n8n.bitsness.vn/webhook/b4d83ecc-a62d-4cdb-9ecb-93013142fe77", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message }),
        })
            .then(response => {
                const contentType = response.headers.get("content-type");
                if (contentType && contentType.includes("application/json")) {
                    return response.json();
                } else {
                    return response.text();
                }
            })
            .then(data => {
                let replyMessage = typeof data === "string" ? data : data.message;
                appendMessage("Bot", replyMessage.replace(/\n/g, "<br>"));
            })
            .catch(error => {
                appendMessage("Bot", "Lỗi khi kết nối với hệ thống.");
            });
    }

    function appendMessage(sender, message) {
        const msgElement = document.createElement("p");
        msgElement.innerHTML = `<strong>${sender}:</strong> ${message}`;
        chatBody.appendChild(msgElement);
        setTimeout(() => {
            chatBody.scrollTop = chatBody.scrollHeight;
        }, 100);
    }
});