document.addEventListener("DOMContentLoaded", () => {
    const chatButton = document.getElementById("chatbot-button");
    const chatPopup = document.getElementById("chatbot-popup");
    const closeChat = document.getElementById("close-chat");
    const sendButton = document.getElementById("send-message");
    const chatInput = document.getElementById("chat-input");
    const chatBody = document.getElementById("chat-body");
    if (!chatButton || !chatPopup || !closeChat || !sendButton || !chatInput || !chatBody) return;

    chatButton.addEventListener("click", () => chatPopup.style.display = "flex");
    closeChat.addEventListener("click", () => chatPopup.style.display = "none");
    sendButton.addEventListener("click", sendMessage);
    chatInput.addEventListener("keydown", event => {
        if (event.key === "Enter") sendMessage();
    });

    async function sendMessage() {
        const message = chatInput.value.trim();
        if (!message || sendButton.disabled) return;
        appendMessage("Bạn", message);
        chatInput.value = "";
        sendButton.disabled = true;

        try {
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
            const headers = {"Content-Type": "application/json"};
            if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
            const response = await fetch("/api/agent/chat", {
                method: "POST",
                headers,
                body: JSON.stringify({message})
            });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const data = await response.json();
            appendMessage(data.requiresEmergencyAction ? "Cảnh báo khẩn cấp" : "Trợ lý", data.reply);
            appendToolData(data.data || []);
        } catch (error) {
            appendMessage("Trợ lý", "Không thể kết nối với hệ thống. Vui lòng thử lại sau.");
        } finally {
            sendButton.disabled = false;
            chatInput.focus();
        }
    }

    function appendMessage(sender, message) {
        const row = document.createElement("p");
        const label = document.createElement("strong");
        label.textContent = `${sender}: `;
        row.append(label, document.createTextNode(message));
        chatBody.appendChild(row);
        chatBody.scrollTop = chatBody.scrollHeight;
    }

    function appendToolData(items) {
        items.forEach(item => {
            const card = document.createElement("div");
            card.className = "agent-result-card";
            Object.entries(item).forEach(([key, value]) => {
                if (value === null || value === undefined || typeof value === "object") return;
                const line = document.createElement("div");
                line.textContent = `${key}: ${value}`;
                card.appendChild(line);
            });
            chatBody.appendChild(card);
        });
        chatBody.scrollTop = chatBody.scrollHeight;
    }
});
