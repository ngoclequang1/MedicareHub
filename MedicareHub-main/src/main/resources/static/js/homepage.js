// Khi load trang, ẩn modal
document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('modal');
    if (modal) {
        modal.style.display = 'none';
    }
    const params = new URLSearchParams(window.location.search);
    if (params.has('showLogin')) {
        showForm('login');
    }
    if (params.has('register')) {
        showForm('register')
    }
    if (params.has('showForgot')) {
        showForm('forgot')
    }
});

// Hiển thị modal với 3 form: login, register, forgot
function showForm(formType) {
    const modal = document.getElementById('modal');
    // Ẩn tất cả form trong modal
    document.querySelectorAll('.form-container').forEach(form => {
        form.style.display = 'none';
    });
    // Hiển thị modal
    modal.style.display = 'flex';
    // Hiển thị form tương ứng
    if (formType === 'login') {
        document.getElementById('login-form').style.display = 'block';
    } else if (formType === 'register') {
        document.getElementById('register-form').style.display = 'block';
    } else if (formType === 'forgot') {
        document.getElementById('forgot-form').style.display = 'block';
    }
}

// Ẩn modal
// Ẩn modal và reset form
function hideForm() {
    const modal = document.getElementById('modal');
    if (modal) {
        modal.style.display = 'none';
        history.replaceState(null, null, window.location.pathname);
    }

    // Xóa nội dung trong tất cả các input field
    document.querySelectorAll("#register-form input, #login-form input, #forgot-form input").forEach((el) => {
        el.value = ""; // Reset giá trị input
        el.classList.remove("error"); // Loại bỏ class lỗi nếu có
    });

    // Xóa nội dung thông báo lỗi và ẩn các phần tử có class error-message
    document.querySelectorAll(".error-message").forEach((el) => {
        el.innerHTML = "";
        el.style.display = "none";
    });
}

window.onclick = function(event) {
    const modal = document.getElementById('modal');
    if (modal && event.target === modal) {
        hideForm();
    }
};


// Smooth scroll function
function smoothScroll(event, sectionId) {
    const section = document.getElementById(sectionId);
    if (section) {
        section.scrollIntoView({ behavior: 'smooth' }); // Scroll smoothly to the section
    } else {
        console.warn(`Section ${sectionId} not found`);
    }
}

