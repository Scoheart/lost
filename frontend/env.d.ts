/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_ALLOW_ADMIN_ACCESS: string
  // more env variables...
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
