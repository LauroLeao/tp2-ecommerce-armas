import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Observable, catchError, debounceTime, distinctUntilChanged, map, of, switchMap, tap } from 'rxjs';
import { Cliente } from '../../models/cliente.model';
import { Pedido, StatusPedido } from '../../models/pedido.model';
import { Produto } from '../../models/produto.model';
import { ClienteService } from '../../services/cliente.service';
import { PedidoService } from '../../services/pedido.service';
import { ProdutoService } from '../../services/produto.service';

interface LinhaNova { produto: Produto; quantidade: number; }

@Component({
  selector: 'app-pedido-admin',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, MatAutocompleteModule, MatButtonModule,
    MatFormFieldModule, MatIconModule, MatInputModule, MatSelectModule],
  templateUrl: './pedido-admin.html',
  styleUrl: './pedido-admin.css',
})
export class PedidoAdmin implements OnInit {
  private readonly changeDetector = inject(ChangeDetectorRef);
  readonly buscaClienteFiltro = new FormControl<Cliente | string>('', { nonNullable: true });
  readonly buscaClienteNovo = new FormControl<Cliente | string>('', { nonNullable: true });
  sugestoesFiltro$!: Observable<Cliente[]>;
  sugestoesNovo$!: Observable<Cliente[]>;
  pedidos: Pedido[] = [];
  selecionado: Pedido | null = null;
  clienteFiltro: number | null = null;
  statusFiltro: 'TODOS' | StatusPedido = 'TODOS';
  novoAberto = false;
  clienteNovoId: number | null = null;
  buscaProduto = '';
  produtosEncontrados: Produto[] = [];
  itensNovos: LinhaNova[] = [];
  carregando = true;
  carregandoProdutos = false;
  salvando = false;
  alterandoStatus = false;
  erro = '';
  aviso = '';

  constructor(
    private route: ActivatedRoute,
    private clienteService: ClienteService,
    private produtoService: ProdutoService,
    private pedidoService: PedidoService,
  ) {}

  get pedidosVisiveis(): Pedido[] {
    return this.statusFiltro === 'TODOS'
      ? this.pedidos : this.pedidos.filter((pedido) => pedido.status === this.statusFiltro);
  }
  get totalNovo(): number {
    return this.itensNovos.reduce((total, item) => total + item.produto.preco * item.quantidade, 0);
  }

  ngOnInit(): void {
    this.sugestoesFiltro$ = this.buscaClienteFiltro.valueChanges.pipe(
      tap((valor) => {
        if (typeof valor === 'string' && this.clienteFiltro !== null) {
          this.clienteFiltro = null;
          this.carregarPedidos();
        }
      }),
      debounceTime(250),
      map((valor) => typeof valor === 'string' ? valor.trim() : ''),
      distinctUntilChanged(),
      switchMap((nome) => nome.length >= 2
        ? this.clienteService.sugerir(nome).pipe(catchError(() => of([])))
        : of([])),
    );
    this.sugestoesNovo$ = this.buscaClienteNovo.valueChanges.pipe(
      tap((valor) => { if (typeof valor === 'string') this.clienteNovoId = null; }),
      debounceTime(250),
      map((valor) => typeof valor === 'string' ? valor.trim() : ''),
      distinctUntilChanged(),
      switchMap((nome) => nome.length >= 2
        ? this.clienteService.sugerir(nome).pipe(catchError(() => of([])))
        : of([])),
    );
    this.route.queryParamMap.subscribe((params) => {
      const id = Number(params.get('clienteId'));
      this.clienteFiltro = Number.isInteger(id) && id > 0 ? id : null;
      if (this.clienteFiltro !== null) {
        this.clienteService.buscarPorId(this.clienteFiltro).subscribe({
          next: (cliente) => this.buscaClienteFiltro.setValue(cliente, { emitEvent: false }),
          error: () => this.limparFiltroCliente(),
        });
      } else {
        this.buscaClienteFiltro.setValue('', { emitEvent: false });
      }
      this.carregarPedidos();
    });
  }

  exibirCliente(valor: Cliente | string | null): string {
    return typeof valor === 'string' ? valor : valor?.nome ?? '';
  }
  selecionarClienteFiltro(cliente: Cliente): void {
    this.clienteFiltro = cliente.id;
    this.carregarPedidos();
  }
  limparFiltroCliente(): void {
    this.clienteFiltro = null;
    this.buscaClienteFiltro.setValue('', { emitEvent: false });
    this.carregarPedidos();
  }
  selecionarClienteNovo(cliente: Cliente): void { this.clienteNovoId = cliente.id; }
  filtrarStatus(status: 'TODOS' | StatusPedido): void {
    this.statusFiltro = status;
    this.selecionado = this.pedidosVisiveis.find((pedido) => pedido.id === this.selecionado?.id)
      ?? this.pedidosVisiveis[0] ?? null;
  }
  carregarPedidos(): void {
    this.carregando = true;
    this.pedidoService.listar(this.clienteFiltro ?? undefined).subscribe({
      next: (pedidos) => {
        this.pedidos = pedidos;
        this.selecionado = this.pedidosVisiveis.find((pedido) => pedido.id === this.selecionado?.id)
          ?? this.pedidosVisiveis[0] ?? null;
        this.carregando = false;
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.erro = 'Não foi possível carregar os pedidos. Verifique o back-end.';
        this.carregando = false;
        this.changeDetector.markForCheck();
      },
    });
  }

  abrirNovo(): void {
    this.novoAberto = true;
    const clienteSelecionado = this.buscaClienteFiltro.value;
    this.clienteNovoId = typeof clienteSelecionado !== 'string'
      && clienteSelecionado.id === this.clienteFiltro ? clienteSelecionado.id : null;
    this.buscaClienteNovo.setValue(
      this.clienteNovoId === null ? '' : clienteSelecionado,
      { emitEvent: false },
    );
    this.itensNovos = [];
    this.buscaProduto = '';
    this.erro = '';
    this.aviso = '';
    this.buscarProdutos();
    setTimeout(() => document.getElementById('novo-pedido')?.scrollIntoView({ block: 'start' }), 0);
  }
  buscarProdutos(): void {
    this.carregandoProdutos = true;
    this.produtoService.listar(this.buscaProduto.trim(), 0, 20).subscribe({
      next: (resposta) => {
        this.produtosEncontrados = resposta.items;
        this.carregandoProdutos = false;
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.erro = 'Não foi possível carregar os produtos.';
        this.carregandoProdutos = false;
        this.changeDetector.markForCheck();
      },
    });
  }
  adicionarProduto(produto: Produto): void {
    if (!produto.id || produto.estoque <= 0 || this.itensNovos.some((item) => item.produto.id === produto.id)) return;
    this.itensNovos = [...this.itensNovos, { produto, quantidade: 1 }];
  }
  definirQuantidade(item: LinhaNova, event: Event): void {
    const input = event.target as HTMLInputElement;
    const numero = Number(input.value);
    item.quantidade = Number.isInteger(numero) && numero > 0 ? Math.min(numero, item.produto.estoque) : 1;
    input.value = String(item.quantidade);
  }
  retirarProduto(produtoId: number): void {
    this.itensNovos = this.itensNovos.filter((item) => item.produto.id !== produtoId);
  }
  salvarPedido(): void {
    if (this.salvando) return;
    if (!this.clienteNovoId || !this.itensNovos.length) {
      this.erro = 'Selecione um cliente e ao menos um produto.';
      return;
    }
    this.salvando = true;
    this.erro = '';
    this.pedidoService.cadastrar({
      clienteId: this.clienteNovoId,
      itens: this.itensNovos.map((item) => ({ produtoId: item.produto.id!, quantidade: item.quantidade })),
    }).subscribe({
      next: (pedido) => {
        this.salvando = false;
        this.novoAberto = false;
        this.selecionado = pedido;
        this.statusFiltro = 'TODOS';
        this.clienteFiltro = null;
        this.buscaClienteFiltro.setValue('', { emitEvent: false });
        this.aviso = 'Pedido registrado como pendente. Confirme-o para descontar o estoque.';
        this.carregarPedidos();
      },
      error: (resposta) => {
        this.salvando = false;
        this.erro = resposta.error?.mensagem ?? 'Não foi possível registrar o pedido.';
        this.changeDetector.markForCheck();
      },
    });
  }
  alterarStatus(status: StatusPedido): void {
    if (!this.selecionado || this.alterandoStatus) return;
    const acao = status === 'CONFIRMADO' ? 'confirmar' : 'cancelar';
    if (!confirm(`Deseja ${acao} o pedido #${this.selecionado.id}?`)) return;
    this.alterandoStatus = true;
    this.erro = '';
    this.pedidoService.alterarStatus(this.selecionado.id, status).subscribe({
      next: (pedido) => {
        this.selecionado = pedido;
        this.alterandoStatus = false;
        this.aviso = status === 'CONFIRMADO'
          ? 'Pedido confirmado. O estoque foi atualizado.'
          : 'Pedido cancelado. O estoque foi devolvido quando necessário.';
        this.carregarPedidos();
      },
      error: (resposta) => {
        this.alterandoStatus = false;
        this.erro = resposta.error?.mensagem ?? 'Não foi possível alterar o pedido.';
        this.changeDetector.markForCheck();
      },
    });
  }
}
