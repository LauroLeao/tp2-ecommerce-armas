import { CurrencyPipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { ProdutoService } from '../../../services/produto.service';
import { Produto } from '../../../models/produto.model';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

@Component({
  selector: 'app-produto-list',
  imports: [
    CurrencyPipe,
    MatTableModule,
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    RouterLink,
  ],
  templateUrl: './produto-list.html',
  styleUrl: './produto-list.css',
})
export class ProdutoList implements OnInit, OnDestroy {
  displayedColumns = ['codigo', 'nome', 'categoria', 'preco', 'estoque', 'status', 'acao'];
  produtos: Produto[] = [];
  filtroNome = '';
  page = 0;
  pageSize = 10;
  totalItems = 0;
  carregando = true;
  erro = '';
  excluindoId?: number;
  private requisicao?: Subscription;
  private readonly changeDetector = inject(ChangeDetectorRef);

  constructor(private produtoService: ProdutoService) {}
  ngOnInit(): void {
    this.carregarProdutos();
  }
  ngOnDestroy(): void {
    this.requisicao?.unsubscribe();
  }
  get comEstoque(): number {
    return this.produtos.filter((produto) => produto.estoque > 0).length;
  }
  imagemUrl(produto: Produto): string | null {
    return this.produtoService.obterUrlImagem(produto.imagemUrl);
  }
  get semEstoque(): number {
    return this.produtos.filter((produto) => produto.estoque <= 0).length;
  }

  buscar(nome: string): void {
    this.filtroNome = nome.trim();
    this.page = 0;
    this.carregarProdutos();
  }
  limparFiltro(input: HTMLInputElement): void {
    input.value = '';
    this.buscar('');
  }
  mudarPagina(event: PageEvent): void {
    this.page = event.pageIndex;
    this.pageSize = event.pageSize;
    this.carregarProdutos();
  }
  excluir(produto: Produto): void {
    if (
      !produto.id ||
      this.excluindoId ||
      !confirm('Deseja excluir o produto "' + produto.nome + '"?')
    ) {
      return;
    }
    this.excluindoId = produto.id;
    this.produtoService.excluir(produto.id).subscribe({
      next: () => {
        this.excluindoId = undefined;
        this.carregarProdutos();
      },
      error: () => {
        this.excluindoId = undefined;
        this.erro = 'Não foi possível excluir o produto. Tente novamente.';
        this.changeDetector.markForCheck();
      },
    });
  }
  carregarProdutos(): void {
    this.requisicao?.unsubscribe();
    this.carregando = true;
    this.erro = '';
    this.requisicao = this.produtoService
      .listar(this.filtroNome, this.page, this.pageSize)
      .subscribe({
        next: (resposta) => {
          if (resposta.items.length === 0 && resposta.totalItems > 0 && this.page > 0) {
            this.page--;
            this.carregarProdutos();
            return;
          }
          this.produtos = resposta.items;
          this.totalItems = resposta.totalItems;
          this.carregando = false;
          this.changeDetector.markForCheck();
        },
        error: () => {
          this.produtos = [];
          this.totalItems = 0;
          this.carregando = false;
          this.erro =
            'Não foi possível carregar os produtos. Verifique se o back-end está em execução e tente novamente.';
          this.changeDetector.markForCheck();
        },
      });
  }
}
