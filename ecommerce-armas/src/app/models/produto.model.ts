export interface Produto {
  id?: number;
  nome: string;
  descricao: string;
  preco: number;
  estoque: number;
  categoriaId: number;
  categoriaNome: string;
  marcaId?: number | null;
  marcaNome?: string | null;
  modeloId?: number | null;
  modeloNome?: string | null;
  fornecedorId?: number | null;
  fornecedorNome?: string | null;
  imagemUrl?: string | null;
}

export type ProdutoRequest = Pick<Produto, 'nome' | 'descricao' | 'preco' | 'estoque' | 'categoriaId'> & {
  modeloId: number;
  fornecedorId?: number | null;
  imagemBase64?: string;
  removerImagem?: boolean;
};
