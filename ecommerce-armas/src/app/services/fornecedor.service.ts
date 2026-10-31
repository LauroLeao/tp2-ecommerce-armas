import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Fornecedor, FornecedorRequest } from '../models/fornecedor.model';

@Injectable({ providedIn: 'root' })
export class FornecedorService {
  private readonly apiUrl = 'http://localhost:8080/fornecedores';
  constructor(private http: HttpClient) {}

  listar(marcaId?: number): Observable<Fornecedor[]> {
    const params = marcaId == null ? undefined : new HttpParams().set('marcaId', marcaId);
    return this.http.get<Fornecedor[]>(this.apiUrl, { params });
  }
  buscarPorId(id: number): Observable<Fornecedor> { return this.http.get<Fornecedor>(`${this.apiUrl}/${id}`); }
  cadastrar(dados: FornecedorRequest): Observable<Fornecedor> { return this.http.post<Fornecedor>(this.apiUrl, dados); }
  editar(id: number, dados: FornecedorRequest): Observable<Fornecedor> { return this.http.put<Fornecedor>(`${this.apiUrl}/${id}`, dados); }
  excluir(id: number): Observable<void> { return this.http.delete<void>(`${this.apiUrl}/${id}`); }
}
