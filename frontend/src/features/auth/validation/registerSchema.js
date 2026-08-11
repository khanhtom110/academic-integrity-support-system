import { z } from "zod";

export const registerSchema = z
  .object({
    fullName: z.string().trim().min(1, "Vui lòng nhập họ và tên."),

    email: z
      .string()
      .trim()
      .min(1, "Vui lòng nhập email.")
      .email("Email không đúng định dạng."),

    password: z
      .string()
      .min(1, "Vui lòng nhập mật khẩu.")
      .min(8, "Mật khẩu phải có ít nhất 8 ký tự."),

    confirmPassword: z.string().min(1, "Vui lòng xác nhận mật khẩu."),
  })
  .refine((data) => data.password === data.confirmPassword, {
    path: ["confirmPassword"],
    message: "Mật khẩu xác nhận không khớp.",
  });
