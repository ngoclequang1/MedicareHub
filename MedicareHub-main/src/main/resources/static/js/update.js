document.addEventListener('DOMContentLoaded', function () {
    const doctorImage = document.getElementById('doctorImage');
    const patientImage = document.getElementById('patientImage')
    const imageUpload = document.getElementById('imageUpload');

    // When the image is clicked, trigger file input click.
    if (doctorImage && imageUpload) {
        doctorImage.addEventListener('click', function () {
            imageUpload.click();
        });

        // Optional: update the image preview when a new file is selected
        imageUpload.addEventListener('change', function () {
            if (this.files && this.files[0]) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    doctorImage.src = e.target.result;
                }
                reader.readAsDataURL(this.files[0]);
            }
        });
    }

    // When the image is clicked, trigger file input click.
    if (patientImage && imageUpload) {
        patientImage.addEventListener('click', function () {
            imageUpload.click();
        });

        // Optional: update the image preview when a new file is selected
        imageUpload.addEventListener('change', function () {
            if (this.files && this.files[0]) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    doctorImage.src = e.target.result;
                }
                reader.readAsDataURL(this.files[0]);
            }
        });
    }
});
