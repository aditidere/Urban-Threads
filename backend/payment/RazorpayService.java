package com.urbanthreads.backend.payment;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public com.razorpay.Order createRazorpayOrder(double amount) throws RazorpayException {

        RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);

        JSONObject options = new JSONObject();

        options.put("amount", (int) (amount * 100)); // Razorpay uses paisa
        options.put("currency", "INR");
        options.put("receipt", "order_" + System.currentTimeMillis());

        return razorpay.orders.create(options);
    }
}