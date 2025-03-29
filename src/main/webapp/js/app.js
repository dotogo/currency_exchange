$(document).ready(function() {
    // Define the backend host URL
    const host = "http://localhost:8080";

    // Fetch the list of currencies and populate dropdowns and tables
    function requestCurrencies() {
        $.ajax({
            url: `${host}/currencies`,
            type: "GET",
            dataType: "json",
            success: function (data) {
                const tbody = $('.currencies-table tbody');
                tbody.empty();

                // Populate the currencies table
                $.each(data, function(index, currency) {
                    const row = $('<tr></tr>');
                    row.append($('<td></td>').text(currency.code));
                    row.append($('<td></td>').text(currency.name));
                    row.append($('<td></td>').text(currency.sign));
                    tbody.append(row);
                });

                // Populate all dropdowns with currency codes
                const selects = ["#new-rate-base-currency", "#new-rate-target-currency", "#convert-base-currency", "#convert-target-currency"];
                selects.forEach(selector => {
                    const element = $(selector);
                    element.empty();
                    $.each(data, function(index, currency) {
                        element.append(`<option value="${currency.code}">${currency.code}</option>`);
                    });
                });
            },
            error: function (jqXHR) {
                const error = JSON.parse(jqXHR.responseText);
                $('#api-error-toast .toast-body').text(error.message);
                $('#api-error-toast').toast("show");
            }
        });
    }

    requestCurrencies();

    // Handle form submission to add a new currency
    $("#add-currency").submit(function(e) {
        e.preventDefault();

        $.ajax({
            url: `${host}/currencies`,
            type: "POST",
            data: $(this).serialize(),
            success: function() {
                requestCurrencies();
            },
            error: function(jqXHR) {
                const error = JSON.parse(jqXHR.responseText);
                $('#api-error-toast .toast-body').text(error.message);
                $('#api-error-toast').toast("show");
            }
        });
    });

    // Fetch exchange rates and populate the table
    function requestExchangeRates() {
        $.ajax({
            url: `${host}/exchangeRates`,
            type: "GET",
            dataType: "json",
            success: function(response) {
                const tbody = $('.exchange-rates-table tbody');
                tbody.empty();

                // Populate the exchange rates table
                $.each(response, function(index, rate) {
                    const row = $('<tr></tr>');
                    const currency = rate.baseCurrency.code + rate.targetCurrency.code;
                    row.append($('<td></td>').text(currency));
                    row.append($('<td></td>').text(rate.rate));
                    row.append($('<td></td>').html(
                        '<button class="btn btn-secondary btn-sm exchange-rate-edit"' +
                        'data-bs-toggle="modal" data-bs-target="#edit-exchange-rate-modal">Edit</button>'
                    ));
                    tbody.append(row);
                });
            },
            error: function(jqXHR) {
                const error = JSON.parse(jqXHR.responseText);
                $('#api-error-toast .toast-body').text(error.message);
                $('#api-error-toast').toast("show");
            }
        });
    }

    requestExchangeRates();

    // Open the edit modal and populate fields with selected exchange rate data
    $(document).delegate('.exchange-rate-edit', 'click', function() {
        const pair = $(this).closest('tr').find('td:first').text();
        const exchangeRate = $(this).closest('tr').find('td:eq(1)').text();

        $('#edit-exchange-rate-modal .modal-title').text(`Edit ${pair} Exchange Rate`);
        $('#edit-exchange-rate-modal #exchange-rate-input').val(exchangeRate);
    });

    // Handle updating the exchange rate
    $('#edit-exchange-rate-modal .btn-primary').click(function() {
        const pair = $('#edit-exchange-rate-modal .modal-title').text().replace('Edit ', '').replace(' Exchange Rate', '');
        const exchangeRate = $('#edit-exchange-rate-modal #exchange-rate-input').val();

        // Reset the table to display the correct value after validation
        requestExchangeRates();

        // Send the updated exchange rate to the server
        $.ajax({
            url: `${host}/exchangeRate/${pair}`,
            type: "PATCH",
            contentType: "application/x-www-form-urlencoded",
            data: `rate=${exchangeRate}`,
            success: function() {
                requestExchangeRates();
            },
            error: function(jqXHR) {
                const error = JSON.parse(jqXHR.responseText);
                $('#api-error-toast .toast-body').text(error.message);
                $('#api-error-toast').toast("show");
            }
        });

        $('#edit-exchange-rate-modal').modal('hide');
    });

    // Handle adding a new exchange rate
    $("#add-exchange-rate").submit(function(e) {
        e.preventDefault();

        $.ajax({
            url: `${host}/exchangeRates`,
            type: "POST",
            data: $(this).serialize(),
            success: function() {
                requestExchangeRates();
            },
            error: function(jqXHR) {
                const error = JSON.parse(jqXHR.responseText);
                $('#api-error-toast .toast-body').text(error.message);
                $('#api-error-toast').toast("show");
            }
        });
    });

    // Handle currency conversion
    $("#convert").submit(function(e) {
        e.preventDefault();

        const baseCurrency = $("#convert-base-currency").val();
        const targetCurrency = $("#convert-target-currency").val();
        const amount = $("#convert-amount").val();

        $.ajax({
            url: `${host}/exchange?from=${baseCurrency}&to=${targetCurrency}&amount=${amount}`,
            type: "GET",
            success: function(data) {
                $("#convert-converted-amount").val(data.convertedAmount);
            },
            error: function(jqXHR) {
                const error = JSON.parse(jqXHR.responseText);
                $('#api-error-toast .toast-body').text(error.message);
                $('#api-error-toast').toast("show");
            }
        });
    });
});


