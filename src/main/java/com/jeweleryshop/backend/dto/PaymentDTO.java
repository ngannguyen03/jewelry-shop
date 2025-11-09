package com.jeweleryshop.backend.dto;

import java.io.Serializable;

public class PaymentDTO {

    public static class VnPayResponse implements Serializable {

        private String code;
        private String message;
        private String paymentUrl;

        public VnPayResponse() {
        }

        public VnPayResponse(String code, String message, String paymentUrl) {
            this.code = code;
            this.message = message;
            this.paymentUrl = paymentUrl;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getPaymentUrl() {
            return paymentUrl;
        }

        public void setPaymentUrl(String paymentUrl) {
            this.paymentUrl = paymentUrl;
        }

        @Override
        public String toString() {
            return "PaymentDTO.VnPayResponse(code=" + this.getCode() + ", message=" + this.getMessage() + ", paymentUrl=" + this.getPaymentUrl() + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof VnPayResponse)) {
                return false;
            }
            final VnPayResponse other = (VnPayResponse) o;
            if (!other.canEqual((Object) this)) {
                return false;
            }
            final Object this$code = this.getCode();
            final Object other$code = other.getCode();
            if (this$code == null ? other$code != null : !this$code.equals(other$code)) {
                return false;
            }
            final Object this$message = this.getMessage();
            final Object other$message = other.getMessage();
            if (this$message == null ? other$message != null : !this$message.equals(other$message)) {
                return false;
            }
            final Object this$paymentUrl = this.getPaymentUrl();
            final Object other$paymentUrl = other.getPaymentUrl();
            if (this$paymentUrl == null ? other$paymentUrl != null : !this$paymentUrl.equals(other$paymentUrl)) {
                return false;
            }
            return true;
        }

        protected boolean canEqual(Object other) {
            return other instanceof VnPayResponse;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $code = this.getCode();
            result = result * PRIME + ($code == null ? 43 : $code.hashCode());
            final Object $message = this.getMessage();
            result = result * PRIME + ($message == null ? 43 : $message.hashCode());
            final Object $paymentUrl = this.getPaymentUrl();
            result = result * PRIME + ($paymentUrl == null ? 43 : $paymentUrl.hashCode());
            return result;
        }
    }
}
