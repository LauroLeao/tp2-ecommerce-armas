import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Produto, ProdutoRequest } from '../models/produto.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({
  providedIn: 'root',
})
export class ProdutoService {
  private readonly apiUrl = 'http://localhost:8080/produtos';

  constructor(private http: HttpClient) {}

  listar(nome: string, page: number, pageSize: number): Observable<PageResponse<Produto>> {
    const params = new HttpParams().set('nome', nome).set('page', page).set('pageSize', pageSize);
    return this.http.get<PageResponse<Produto>>(this.apiUrl, { params });
  }

  buscarPorId(id: number): Observable<Produto> {
    return this.http.get<Produto>(`${this.apiUrl}/${id}`);
  }

  obterUrlImagem(caminho?: string | null): string | null {
    return caminho ? new URL(caminho, this.apiUrl).href : null;
  }

  cadastrar(produto: ProdutoRequest): Observable<Produto> {
    return this.http.post<Produto>(this.apiUrl, produto);
  }

  editar(id: number, produto: ProdutoRequest): Observable<Produto> {
    return this.http.put<Produto>(`${this.apiUrl}/${id}`, produto);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
