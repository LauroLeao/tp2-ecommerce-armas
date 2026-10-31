import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pedido, PedidoRequest, StatusPedido } from '../models/pedido.model';

@Injectable({ providedIn: 'root' })
export class PedidoService {
  private readonly apiUrl = 'http://localhost:8080/pedidos';

  constructor(private http: HttpClient) {}

  listar(clienteId?: number): Observable<Pedido[]> {
    const params = clienteId ? new HttpParams().set('clienteId', clienteId) : undefined;
    return this.http.get<Pedido[]>(this.apiUrl, { params });
  }

  cadastrar(dados: PedidoRequest): Observable<Pedido> {
    return this.http.post<Pedido>(this.apiUrl, dados);
  }

  alterarStatus(id: number, status: StatusPedido): Observable<Pedido> {
    return this.http.put<Pedido>(`${this.apiUrl}/${id}/status`, { status });
  }
}
