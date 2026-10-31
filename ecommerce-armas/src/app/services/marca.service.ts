import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Marca, MarcaRequest } from '../models/marca.model';

@Injectable({ providedIn: 'root' })
export class MarcaService {
  private readonly apiUrl = 'http://localhost:8080/marcas';
  constructor(private http: HttpClient) {}

  listar(): Observable<Marca[]> { return this.http.get<Marca[]>(this.apiUrl); }
  buscarPorId(id: number): Observable<Marca> { return this.http.get<Marca>(`${this.apiUrl}/${id}`); }
  cadastrar(dados: MarcaRequest): Observable<Marca> { return this.http.post<Marca>(this.apiUrl, dados); }
  editar(id: number, dados: MarcaRequest): Observable<Marca> { return this.http.put<Marca>(`${this.apiUrl}/${id}`, dados); }
  excluir(id: number): Observable<void> { return this.http.delete<void>(`${this.apiUrl}/${id}`); }
}
