export type StatusPedido = 'PENDENTE' | 'CONFIRMADO' | 'CANCELADO';

export interface ItemPedido {
  produtoId: number;
  nomeProduto: string;
  quantidade: number;
  precoUnitario: number;
  subtotal: number;
}

export interface Pedido {
  id: number;
  clienteId: number;
  clienteNome: string;
  criadoEm: string;
  status: StatusPedido;
  total: number;
  itens: ItemPedido[];
}

export interface PedidoRequest {
  clienteId: number;
  itens: { produtoId: number; quantidade: number }[];
}
