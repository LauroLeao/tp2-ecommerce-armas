export interface Modelo {
  id: number;
  nome: string;
  marcaId: number;
  marcaNome: string;
}

export type ModeloRequest = Pick<Modelo, 'nome' | 'marcaId'>;
