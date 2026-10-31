export interface Marca {
  id: number;
  nome: string;
}

export type MarcaRequest = Pick<Marca, 'nome'>;
