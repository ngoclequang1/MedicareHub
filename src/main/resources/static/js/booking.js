document.addEventListener("DOMContentLoaded", () =>{
    const deleteBtn = document.querySelector('.delete-text');
    const popup = document.getElementById('delete-popup');
    const cancelBtn = document.getElementById('cancel-delete');
    const confirmBtn = document.getElementById('confirm-delete');

    deleteBtn.addEventListener('click', () => {
        popup.style.display = 'flex';
    });

    cancelBtn.addEventListener('click', () => {
        popup.style.display = 'none';
    });

})
