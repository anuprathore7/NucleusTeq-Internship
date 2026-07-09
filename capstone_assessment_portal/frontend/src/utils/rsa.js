import JSEncrypt from "jsencrypt";
import axiosInstance from "../api/axios";
import { ENDPOINTS } from "./constants";

let cachedPublicKey = null;

const getPublicKey = async () => {

  if (cachedPublicKey) {
    return cachedPublicKey;
  }

  const response = await axiosInstance.get(
    ENDPOINTS.PUBLIC_KEY
  );

  cachedPublicKey = response.data.public_Key;

  return cachedPublicKey;
};

export const encryptPassword = async (password) => {

  const publicKey = await getPublicKey();

  const encryptor = new JSEncrypt();

  encryptor.setPublicKey(publicKey);

  const encryptedPassword = encryptor.encrypt(password);

  if (!encryptedPassword) {
    throw new Error("Password encryption failed.");
  }

  return encryptedPassword;
};