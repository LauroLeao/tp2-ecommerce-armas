export interface ClienteRequest {
  nome: string;
  email: string;
  telefone: string;
  cpf: string;
  cep: string | null;
  logradouro: string;
  numero: string;
  complemento: string;
  bairro: string;
  municipioNome: string;
  municipioCodigoIbge: string;
  estadoSigla: string;
  estadoNome: string;
  fotoBase64?: string;
  removerFoto?: boolean;
}

export interface Cliente {
  id: number;
  nome: string;
  email: string;
  telefone: string;
  endereco: string;
  cpf: string | null;
  cep: string | null;
  logradouro: string | null;
  numero: string | null;
  complemento: string | null;
  bairro: string | null;
  municipioNome: string | null;
  municipioCodigoIbge: string | null;
  estadoSigla: string | null;
  estadoNome: string | null;
  fotoUrl: string | null;
}
