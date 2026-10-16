export interface Produto {
  id?: number;
  nome: string;
  descricao: string;
  preco: number;
  estoque: number;
  categoriaId: number;
  categoriaNome: string;
  imagemUrl?: string | null;
}

export type ProdutoRequest = Omit<Produto, 'id' | 'categoriaNome' | 'imagemUrl'> & {
  imagemBase64?: string;
  removerImagem?: boolean;
};
