const toggleSidebar = () => {
    if ($('.sidebar').is(":visible")) {
        $(".sidebar").hide();
        $(".content").css("margin-left", "0%");
    } else {
        $(".sidebar").show();
        $(".content").css("margin-left", "20%");
    }
};



//payment

const paymentStart = () => {
	let amount = $("#amount").val();
	if (amount == '' || amount == null) {
		swal("Failed !!", "amount is required !!", "error");
		return;
	}
	//ajax to send request to server to create order
	$.ajax({
		url: '/user/create_order',
		data: JSON.stringify({ amount: amount, info: 'order_request' }),
		contentType: 'application/json ',
		type: 'POST',
		dataType: 'json',
		success: function(response) {
			console.log(response);

			if (response.status == 'created') {
				let options = {
					key: 'rzp_test_WOZCIvlkIlIEDA',
					amount: response.amount,
					currency: 'INR',
					name: 'Contact Manager',
					description: 'Charges',
					image: "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQcF5WX9OsR2TYeV1un_BIsqg9ddOme2iTHCQ&usqp=CAU",
					order_id: response.id,
					handler: function(response) {
						console.log(response);
						console.log(response.razorpay_payment_id);
						console.log(response.razorpay_order_id);
						console.log(response.razorpay_signature);
						console.log('Payment Successfull !!');
						updatePaymentOnServer(response.razorpay_payment_id, response.razorpay_order_id, 'paid')
						swal("Good job!", "congrates !! Payment Successful !!", "success")
					},
					prefill: {
						name: "",
						email: "",
						contact: ""
					},
					notes: {
						address: "Contact Manager."
					},
					theme: {
						color: "#3399cc",
					},
				};
				let rzp = new Razorpay(options);
				rzp.on('payment.failed', function(response) {
					console.log(response.error.code);
					console.log(response.error.description);
					console.log(response.error.source);
					console.log(response.error.step);
					console.log(response.error.reason);
					console.log(response.error.metadata.order_id);
					console.log(response.error.metadata.payment_id);
					swal("Failed !!", "oops payment failed !!", "error");
					return;
				});

				rzp.open();
			}
		},
		error: function(error) {
			swal("Failed !!", "oops payment failed !!", "error");
		}
	})
};

function updatePaymentOnServer(payment_id, order_id, status) {
	$.ajax({
		url: '/user/update_order',
		data: JSON.stringify({ payment_id: payment_id, order_id: order_id, status: status }),
		contentType: 'application/json ',
		type: 'POST',
		dataType: 'json',
		success: function(response) {
			swal("Good job!", "congrates !! Payment Successful !!", "success")
		}, error: function(error) {
			swal("Failed !!", "Your payment is successful , but we did not get on server , we will contact you as soon as possible", "error");
		}
	})
}
