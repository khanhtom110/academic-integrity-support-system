import { z } from "zod";

export const otpSchema = z.object({
  otp: z
    .string()
    .length(6, "Mã OTP phải gồm 6 chữ số.")
    .regex(/^\d+$/, "OTP chỉ được chứa chữ số."),
});
