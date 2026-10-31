import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Modelo, ModeloRequest } from '../models/modelo.model';

@Injectable({ providedIn: 'root' })
export class ModeloService {
  private readonly apiUrl = 'http://localhost:8080/modelos';
  constructor(private http: HttpClient) {}

  listar(marcaId?: number): Observable<Modelo[]> {
    const params = marcaId == null ? undefined : new HttpParams().set('marcaId', marcaId);
    return this.http.get<Modelo[]>(this.apiUrl, { params });
  }
  buscarPorId(id: number): Observable<Modelo> { return this.http.get<Modelo>(`${this.apiUrl}/${id}`); }
  cadastrar(dados: ModeloRequest): Observable<Modelo> { return this.http.post<Modelo>(this.apiUrl, dados); }
  editar(id: number, dados: ModeloRequest): Observable<Modelo> { return this.http.put<Modelo>(`${this.apiUrl}/${id}`, dados); }
  excluir(id: number): Observable<void> { return this.http.delete<void>(`${this.apiUrl}/${id}`); }
}
