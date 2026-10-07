/**
 * AutoDrive Car Rental - Client Interactive Scripts
 */

document.addEventListener('DOMContentLoaded', function () {
    initDatePickers();
    initAvailabilityChecker();
    initPriceCalculator();
    initSandboxPaymentToggle();
});

// Configure sensible date limits for booking forms
function initDatePickers() {
    const today = new Date().toISOString().split('T')[0];
    const pickupInput = document.getElementById('pickupDate');
    const returnInput = document.getElementById('returnDate');

    if (pickupInput) {
        if (!pickupInput.getAttribute('min')) {
            pickupInput.setAttribute('min', today);
        }

        pickupInput.addEventListener('change', function () {
            if (returnInput) {
                returnInput.setAttribute('min', this.value);
                if (returnInput.value && returnInput.value < this.value) {
                    returnInput.value = this.value;
                }
                triggerAvailabilityCheck();
                updateCalculatedTotal();
            }
        });
    }

    if (returnInput) {
        returnInput.addEventListener('change', function () {
            triggerAvailabilityCheck();
            updateCalculatedTotal();
        });
    }
}

// Live availability checking via AJAX
function initAvailabilityChecker() {
    const checkBtn = document.getElementById('btnCheckAvailability');
    if (checkBtn) {
        checkBtn.addEventListener('click', function (e) {
            e.preventDefault();
            triggerAvailabilityCheck();
        });
    }
}

function triggerAvailabilityCheck() {
    const vehicleIdInput = document.getElementById('vehicleId');
    const pickupInput = document.getElementById('pickupDate');
    const returnInput = document.getElementById('returnDate');
    const resultBox = document.getElementById('availabilityResult');

    if (!vehicleIdInput || !pickupInput || !returnInput || !resultBox) {
        return;
    }

    const vehicleId = vehicleIdInput.value;
    const pickupDate = pickupInput.value;
    const returnDate = returnInput.value;

    if (!pickupDate || !returnDate) {
        resultBox.innerHTML = '<div class="alert alert-warning py-2 mb-0">Please select both pickup and return dates.</div>';
        return;
    }

    resultBox.innerHTML = '<div class="text-muted small"><i class="fas fa-spinner fa-spin me-1"></i> Checking schedule availability...</div>';

    fetch(`/vehicles/availability?vehicleId=${vehicleId}&pickupDate=${pickupDate}&returnDate=${returnDate}`)
        .then(response => response.json())
        .then(data => {
            if (data.available) {
                resultBox.innerHTML = `<div class="alert alert-success py-2 mb-0"><i class="fas fa-check-circle me-1"></i> ${data.message}</div>`;
                const bookNowBtn = document.getElementById('btnProceedBooking');
                if (bookNowBtn) bookNowBtn.classList.remove('disabled');
            } else {
                resultBox.innerHTML = `<div class="alert alert-danger py-2 mb-0"><i class="fas fa-times-circle me-1"></i> ${data.message}</div>`;
                const bookNowBtn = document.getElementById('btnProceedBooking');
                if (bookNowBtn) bookNowBtn.classList.add('disabled');
            }
        })
        .catch(err => {
            resultBox.innerHTML = '<div class="alert alert-danger py-2 mb-0">Unable to check vehicle schedule at this moment.</div>';
        });
}

// Interactive Price Calculation on booking pages
function initPriceCalculator() {
    updateCalculatedTotal();
}

function updateCalculatedTotal() {
    const dailyPriceEl = document.getElementById('pricePerDayVal');
    const depositEl = document.getElementById('securityDepositVal');
    const pickupInput = document.getElementById('pickupDate');
    const returnInput = document.getElementById('returnDate');
    const daysDisplay = document.getElementById('rentalDaysDisplay');
    const subtotalDisplay = document.getElementById('rentalSubtotalDisplay');
    const totalDisplay = document.getElementById('totalAmountDisplay');

    if (!dailyPriceEl || !pickupInput || !returnInput || !daysDisplay || !totalDisplay) {
        return;
    }

    const dailyPrice = parseFloat(dailyPriceEl.value) || 0;
    const deposit = depositEl ? (parseFloat(depositEl.value) || 0) : 0;

    const pDate = new Date(pickupInput.value);
    const rDate = new Date(returnInput.value);

    if (isNaN(pDate.getTime()) || isNaN(rDate.getTime())) {
        return;
    }

    const diffTime = rDate.getTime() - pDate.getTime();
    let days = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    if (days <= 0) days = 1;

    const subtotal = dailyPrice * days;
    const total = subtotal + deposit;

    daysDisplay.textContent = days + (days === 1 ? ' Day' : ' Days');
    if (subtotalDisplay) subtotalDisplay.textContent = '$' + subtotal.toFixed(2);
    totalDisplay.textContent = '$' + total.toFixed(2);
}

// Payment method and sandbox simulation selector
function initSandboxPaymentToggle() {
    const outcomeRadios = document.querySelectorAll('input[name="testOutcome"]');
    const cardForm = document.getElementById('cardDetailsContainer');

    outcomeRadios.forEach(radio => {
        radio.addEventListener('change', function () {
            const notice = document.getElementById('testOutcomeNotice');
            if (notice) {
                if (this.value === 'FAILED') {
                    notice.className = 'alert alert-danger py-2 mt-2';
                    notice.textContent = 'Sandbox Mode: Simulation configured to simulate a declined transaction.';
                } else {
                    notice.className = 'alert alert-success py-2 mt-2';
                    notice.textContent = 'Sandbox Mode: Simulation configured to process payment successfully.';
                }
            }
        });
    });
}
