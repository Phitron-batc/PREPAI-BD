package com.example.data.engine

import com.example.data.model.PaymentProvider
import com.example.data.model.PaymentResult
import com.example.data.model.SubscriptionPlan
import com.example.data.model.SubscriptionTier

/**
 * Interface contract for Bangladesh MFS & Payment Gateways.
 */
interface PaymentGatewayContract {
    suspend fun initiatePayment(plan: SubscriptionPlan, customerPhone: String): PaymentResult
    suspend fun verifyPayment(paymentId: String): PaymentResult
}

class BkashPaymentGateway(
    private val appKey: String = "",
    private val appSecret: String = "",
    private val username: String = ""
) : PaymentGatewayContract {

    fun isMerchantConfigured(): Boolean = appKey.isNotBlank() && appSecret.isNotBlank()

    override suspend fun initiatePayment(plan: SubscriptionPlan, customerPhone: String): PaymentResult {
        if (!isMerchantConfigured()) {
            return PaymentResult.RequiresMerchantSetup(
                provider = PaymentProvider.BKASH,
                message = "bKash Merchant API credentials are not yet configured. Live payments require registration on the bKash Merchant Portal (PGW Sandbox / Production)."
            )
        }
        return PaymentResult.Failed("Network connection to bKash PGW endpoint failed.")
    }

    override suspend fun verifyPayment(paymentId: String): PaymentResult {
        return PaymentResult.Failed("Verification pending merchant token exchange.")
    }
}

class NagadPaymentGateway(
    private val merchantId: String = "",
    private val publicKey: String = "",
    private val privateKey: String = ""
) : PaymentGatewayContract {

    fun isMerchantConfigured(): Boolean = merchantId.isNotBlank() && publicKey.isNotBlank()

    override suspend fun initiatePayment(plan: SubscriptionPlan, customerPhone: String): PaymentResult {
        if (!isMerchantConfigured()) {
            return PaymentResult.RequiresMerchantSetup(
                provider = PaymentProvider.NAGAD,
                message = "Nagad Merchant API credentials are not yet configured. Requires Nagad Merchant Portal onboarding and public/private RSA key pair."
            )
        }
        return PaymentResult.Failed("Network connection to Nagad PGW endpoint failed.")
    }

    override suspend fun verifyPayment(paymentId: String): PaymentResult {
        return PaymentResult.Failed("Verification pending Nagad signature callback.")
    }
}

class SslCommerzPaymentGateway(
    private val storeId: String = "",
    private val storePassword: String = "",
    private val isLive: Boolean = false
) : PaymentGatewayContract {

    fun isMerchantConfigured(): Boolean = storeId.isNotBlank() && storePassword.isNotBlank()

    override suspend fun initiatePayment(plan: SubscriptionPlan, customerPhone: String): PaymentResult {
        if (!isMerchantConfigured()) {
            return PaymentResult.RequiresMerchantSetup(
                provider = PaymentProvider.SSLCOMMERZ,
                message = "SSLCommerz Store ID and Password are not yet configured. Requires active SSLCommerz merchant account."
            )
        }
        return PaymentResult.Failed("Network connection to SSLCommerz gateway failed.")
    }

    override suspend fun verifyPayment(paymentId: String): PaymentResult {
        return PaymentResult.Failed("Validation pending IPN listener verification.")
    }
}

/**
 * Feature gating service enforcing FREE, BASIC, and PREMIUM subscription limits.
 */
object FeatureGatingManager {

    data class GatingCheck(
        val isAllowed: Boolean,
        val requiredTier: SubscriptionTier,
        val messageEn: String,
        val messageBn: String
    )

    fun canAccessAiCopilot(tier: SubscriptionTier): GatingCheck {
        return if (tier == SubscriptionTier.PREMIUM) {
            GatingCheck(true, SubscriptionTier.PREMIUM, "Access granted.", "অ্যাক্সেস অনুমোদিত।")
        } else {
            GatingCheck(
                isAllowed = false,
                requiredTier = SubscriptionTier.PREMIUM,
                messageEn = "AI Copilot deep tutoring and RAG analysis require a PREMIUM subscription.",
                messageBn = "এআই কোপাইলট ডিপ টিউটরিং ও আরএজি ডকুমেন্ট বিশ্লেষণ সুবিধা পেতে প্রিমিয়াম সাবস্ক্রিপশন প্রয়োজন।"
            )
        }
    }

    fun canAccessPersonalizedPlanner(tier: SubscriptionTier): GatingCheck {
        return if (tier == SubscriptionTier.PREMIUM || tier == SubscriptionTier.BASIC) {
            GatingCheck(true, SubscriptionTier.BASIC, "Access granted.", "অ্যাক্সেস অনুমোদিত।")
        } else {
            GatingCheck(
                isAllowed = false,
                requiredTier = SubscriptionTier.BASIC,
                messageEn = "Personalized Study Planner is available on BASIC and PREMIUM plans.",
                messageBn = "পার্সোনালাইজড স্টাডি প্ল্যানার সুবিধা পেতে বেসিক বা প্রিমিয়াম সাবস্ক্রিপশন প্রয়োজন।"
            )
        }
    }

    fun canAccessAllMockExams(tier: SubscriptionTier): GatingCheck {
        return if (tier != SubscriptionTier.FREE) {
            GatingCheck(true, SubscriptionTier.BASIC, "Access granted.", "অ্যাক্সেস অনুমোদিত।")
        } else {
            GatingCheck(
                isAllowed = false,
                requiredTier = SubscriptionTier.BASIC,
                messageEn = "Full 200-mark BCS Model Tests require a BASIC or PREMIUM plan.",
                messageBn = "সম্পূর্ণ ২০০ নম্বরের বিসিএস স্পেশাল মডেল টেস্টের জন্য বেসিক বা প্রিমিয়াম সাবস্ক্রিপশন প্রয়োজন।"
            )
        }
    }
}
