export interface Fornecedor {
  id: number;
  nome: string;
  email: string;
  telefone: string;
  marcaId: number;
  marcaNome: string;
}

export type FornecedorRequest = Pick<Fornecedor, 'nome' | 'email' | 'telefone' | 'marcaId'>;
