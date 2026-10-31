import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, PLATFORM_ID, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../services/auth.service';
import { Cliente } from '../../models/cliente.model';
import { Pedido } from '../../models/pedido.model';

interface ResumoAdmin { produtos: number; semEstoque: number; clientes: number; pedidosPendentes: number; pedidosConfirmados: number; }
interface ResumoCliente { pedidos: number; pendentes: number; recentes: Pedido[]; }

@Component({
  selector: 'app-painel',
  imports: [CommonModule, RouterLink, MatButtonModule, MatIconModule],
  templateUrl: './painel.html',
  styleUrl: './painel.css',
})
export class Painel implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly cd = inject(ChangeDetectorRef);
  private readonly browser = isPlatformBrowser(inject(PLATFORM_ID));
  readonly auth = inject(AuthService);
  resumoAdmin: ResumoAdmin | null = null;
  resumoCliente: ResumoCliente | null = null;
  cliente: Cliente | null = null;
  erro = '';
  carregando = true;

  ngOnInit(): void {
    if (!this.browser) return;
    const usuario = this.auth.usuario();
    if (usuario) this.carregar(usuario.perfil);
    else this.auth.carregar().subscribe((sessao) => sessao && this.carregar(sessao.perfil));
  }

  carregar(perfil: 'ADMIN' | 'CLIENTE'): void {
    this.erro = '';
    this.carregando = true;
    if (perfil === 'ADMIN') {
      this.http.get<ResumoAdmin>('http://localhost:8080/painel/admin').subscribe({
        next: (dados) => { this.resumoAdmin = dados; this.carregando = false; this.cd.markForCheck(); },
        error: () => this.falhou(),
      });
    } else {
      this.http.get<ResumoCliente>('http://localhost:8080/painel/cliente').subscribe({
        next: (dados) => { this.resumoCliente = dados; this.carregando = false; this.cd.markForCheck(); },
        error: () => this.falhou(),
      });
      this.auth.meuCadastro().subscribe({
        next: (cliente) => { this.cliente = cliente; this.cd.markForCheck(); },
        error: () => this.cd.markForCheck(),
      });
    }
  }

  private falhou(): void {
    this.erro = 'Não foi possível carregar o painel. Verifique a conexão e tente novamente.';
    this.carregando = false;
    this.cd.markForCheck();
  }
}
