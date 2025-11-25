import { apiFetch, type ApiResponse } from "./api";

export interface VendorSummary {
  id: string;
  companyName: string | null;
  firstName?: string | null;
  lastName?: string | null;
  email?: string | null;
}

const unwrap = async <T>(promise: Promise<ApiResponse<T>>) => {
  const res = await promise;
  return res.data;
};

export const userApi = {
  listVendors: () => unwrap(apiFetch<VendorSummary[]>("/api/users/vendors")),
};
