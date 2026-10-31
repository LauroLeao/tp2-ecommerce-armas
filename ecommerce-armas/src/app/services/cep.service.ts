import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CepConsulta } from '../models/cep.model';

@Injectable({ providedIn: 'root' })
export class CepService {
  constructor(private http: HttpClient) {}

  consultar(cep: string): Observable<CepConsulta> {
    return this.http.get<CepConsulta>(`https://viacep.com.br/ws/${cep}/json/`);
  }
}
