import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EstadoIbge, MunicipioIbge } from '../models/localidade.model';

@Injectable({ providedIn: 'root' })
export class LocalidadeService {
  private readonly apiUrl = 'https://servicodados.ibge.gov.br/api/v1/localidades';

  constructor(private http: HttpClient) {}

  listarEstados(): Observable<EstadoIbge[]> {
    return this.http.get<EstadoIbge[]>(`${this.apiUrl}/estados?orderBy=nome`);
  }

  listarMunicipios(uf: string): Observable<MunicipioIbge[]> {
    return this.http.get<MunicipioIbge[]>(`${this.apiUrl}/estados/${uf}/municipios?orderBy=nome`);
  }
}
