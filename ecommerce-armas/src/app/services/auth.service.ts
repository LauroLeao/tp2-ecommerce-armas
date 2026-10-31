import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Observable, catchError, of, switchMap, tap } from 'rxjs';
import { Cliente, ClienteRequest } from '../models/cliente.model';

export interface Sessao {
  email: string;
  perfil: 'ADMIN' | 'CLIENTE';
  clienteId: number | null;
  nome: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly browser = isPlatformBrowser(inject(PLATFORM_ID));
  private readonly api = 'http://localhost:8080/auth';
  readonly usuario = signal<Sessao | null>(null);

  carregar(): Observable<Sessao | null> {
    if (!this.browser) return of(null);
    return this.http.get<Sessao>(`${this.api}/me`).pipe(
      tap((usuario) => this.usuario.set(usuario)),
      catchError(() => {
        this.usuario.set(null);
        return of(null);
      }),
    );
  }

  entrar(email: string, senha: string): Observable<Sessao | null> {
    const dados = new URLSearchParams({ j_username: email.trim().toLowerCase(), j_password: senha });
    return this.http.post('http://localhost:8080/j_security_check', dados.toString(), {
      headers: new HttpHeaders({ 'Content-Type': 'application/x-www-form-urlencoded' }),
      responseType: 'text',
    }).pipe(switchMap(() => this.carregar()));
  }

  cadastrar(cliente: ClienteRequest, senha: string): Observable<Cliente> {
    return this.http.post<Cliente>(`${this.api}/cadastro`, { cliente, senha });
  }

  meuCadastro(): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.api}/me/cliente`);
  }

  atualizarMeuCadastro(cliente: ClienteRequest): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.api}/me/cliente`, cliente).pipe(
      tap((atualizado) => {
        const atual = this.usuario();
        if (atual) this.usuario.set({ ...atual, nome: atualizado.nome, email: atualizado.email });
      }),
    );
  }

  sair(): Observable<void> {
    return this.http.post<void>(`${this.api}/logout`, {}).pipe(
      tap(() => this.usuario.set(null)),
    );
  }
}
