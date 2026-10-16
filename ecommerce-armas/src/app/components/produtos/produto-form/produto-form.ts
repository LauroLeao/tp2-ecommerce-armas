import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { ProdutoService } from '../../../services/produto.service';
import { CategoriaService } from '../../../services/categoria.service';
import { Categoria } from '../../../models/categoria.model';
import { Produto, ProdutoRequest } from '../../../models/produto.model';

@Component({
  selector: 'app-produto-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
  ],
  templateUrl: './produto-form.html',
  styleUrl: './produto-form.css',
})
export class ProdutoForm implements OnInit, OnDestroy {
  formGroup!: FormGroup;
  categorias: Categoria[] = [];
  erroCategoria = '';
  erro = '';
  salvando = false;
  excluindo = false;
  cadastrandoCategoria = false;
  carregandoCategorias = true;
  readonly imagemPreview = signal<string | null>(null);
  readonly nomeImagem = signal('');
  readonly erroImagem = signal('');
  readonly carregandoImagem = signal(false);
  private imagemBase64: string | null = null;
  private removerImagem = false;
  private imagemOperacao = 0;
  private readonly tamanhoMaximoImagem = 5 * 1024 * 1024;
  private readonly pixelsMaximosImagem = 25_000_000;
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

  constructor(
    private formBuilder: FormBuilder,
    private produtoService: ProdutoService,
    private categoriaService: CategoriaService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
  ) {}

  get editando(): boolean {
    return !!this.formGroup?.get('id')?.value;
  }

  get ocupado(): boolean {
    return this.salvando || this.excluindo;
  }

  get nomePreview(): string {
    return this.formGroup?.get('nome')?.value?.trim() || 'Nome do produto';
  }

  get categoriaPreview(): string {
    const id = Number(this.formGroup?.get('categoriaId')?.value);
    return (
      this.categorias.find((categoria) => categoria.id === id)?.nome || 'Selecione uma categoria'
    );
  }

  get precoPreview(): string {
    const preco = Number(this.formGroup?.get('preco')?.value);
    return this.moeda.format(Number.isFinite(preco) ? Math.max(0, preco) : 0);
  }

  get estoquePreview(): number {
    const estoque = Number(this.formGroup?.get('estoque')?.value);
    return Number.isFinite(estoque) ? Math.max(0, Math.trunc(estoque)) : 0;
  }

  ngOnInit(): void {
    const produto: Produto | undefined = this.activatedRoute.snapshot.data['produto'];
    this.imagemPreview.set(this.produtoService.obterUrlImagem(produto?.imagemUrl));
    this.nomeImagem.set(this.imagemPreview() ? 'Imagem atual do produto' : '');

    this.formGroup = this.formBuilder.group({
      id: [produto?.id ?? null],
      nome: [produto?.nome ?? '', [Validators.required, Validators.pattern(/\S/)]],
      descricao: [produto?.descricao ?? ''],
      preco: [produto?.preco ?? null, [Validators.required, Validators.min(0)]],
      estoque: [
        produto?.estoque ?? null,
        [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)],
      ],
      categoriaId: [produto?.categoriaId ?? null, Validators.required],
    });

    this.categoriaService.listar().subscribe({
      next: (categorias) => {
        this.categorias = categorias;
        this.carregandoCategorias = false;
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.carregandoCategorias = false;
        this.erroCategoria =
          'Não foi possível carregar as categorias. Verifique se o servidor está em execução e atualize a página.';
        this.changeDetector.markForCheck();
      },
    });
  }

  ngOnDestroy(): void {
    this.imagemOperacao++;
  }

  async selecionarImagem(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const arquivo = input.files?.[0];
    input.value = '';
    if (!arquivo || this.ocupado) return;
    const operacao = ++this.imagemOperacao;
    this.erroImagem.set('');
    this.carregandoImagem.set(false);
    if (!['image/jpeg', 'image/png'].includes(arquivo.type)) {
      this.erroImagem.set('Escolha uma imagem JPG ou PNG.');
      return;
    }
    if (arquivo.size > this.tamanhoMaximoImagem) {
      this.erroImagem.set('A imagem deve ter no máximo 5 MB.');
      return;
    }
    this.carregandoImagem.set(true);
    try {
      const dados = await this.lerImagem(arquivo);
      const imagem = new Image();
      imagem.src = dados;
      await imagem.decode();
      if (operacao !== this.imagemOperacao) return;
      if (
        imagem.naturalWidth > 10_000 ||
        imagem.naturalHeight > 10_000 ||
        imagem.naturalWidth * imagem.naturalHeight > this.pixelsMaximosImagem
      ) {
        throw new Error('dimensoes');
      }
      this.imagemBase64 = dados;
      this.imagemPreview.set(dados);
      this.nomeImagem.set(arquivo.name);
      this.removerImagem = false;
      this.formGroup.markAsDirty();
    } catch (erro) {
      if (operacao !== this.imagemOperacao) return;
      this.erroImagem.set(
        erro instanceof Error && erro.message === 'dimensoes'
          ? 'A imagem tem resolução muito alta. Use até 25 megapixels e 10.000 pixels por lado.'
          : 'Não foi possível abrir essa imagem. Escolha outro arquivo JPG ou PNG.',
      );
    } finally {
      if (operacao === this.imagemOperacao) {
        this.carregandoImagem.set(false);
      }
    }
  }

  retirarImagem(): void {
    if (this.ocupado) return;
    this.imagemOperacao++;
    this.imagemBase64 = null;
    this.imagemPreview.set(null);
    this.nomeImagem.set('');
    this.removerImagem = true;
    this.erroImagem.set('');
    this.carregandoImagem.set(false);
    this.formGroup.markAsDirty();
  }

  private lerImagem(arquivo: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const leitor = new FileReader();
      leitor.onload = () =>
        typeof leitor.result === 'string' ? resolve(leitor.result) : reject(new Error('arquivo'));
      leitor.onerror = () => reject(new Error('arquivo'));
      leitor.readAsDataURL(arquivo);
    });
  }

  adicionarCategoria(input: HTMLInputElement): void {
    if (this.cadastrandoCategoria || this.ocupado) return;

    const nome = input.value.trim();
    if (!nome) {
      this.erroCategoria = 'Informe o nome da nova categoria.';
      input.focus();
      return;
    }

    this.erroCategoria = '';
    this.cadastrandoCategoria = true;
    this.categoriaService.cadastrar(nome).subscribe({
      next: (categoria) => {
        this.categorias = [...this.categorias, categoria].sort((a, b) =>
          a.nome.localeCompare(b.nome),
        );
        this.formGroup.patchValue({ categoriaId: categoria.id });
        input.value = '';
        this.cadastrandoCategoria = false;
        this.changeDetector.markForCheck();
      },
      error: (erro) => {
        this.erroCategoria =
          erro.status === 409
            ? 'Essa categoria já existe. Selecione-a na lista.'
            : 'Não foi possível cadastrar a categoria. Tente novamente.';
        this.cadastrandoCategoria = false;
        this.changeDetector.markForCheck();
      },
    });
  }

  limpar(): void {
    if (this.ocupado) return;
    const id = this.formGroup.get('id')?.value;
    this.formGroup.reset({
      id,
      nome: '',
      descricao: '',
      preco: null,
      estoque: null,
      categoriaId: null,
    });
    this.erro = '';
    this.retirarImagem();
  }

  salvar(): void {
    if (this.ocupado || this.cadastrandoCategoria || this.carregandoImagem()) return;
    this.erro = '';
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      this.erro = 'Confira os campos destacados antes de salvar.';
      return;
    }

    const valores = this.formGroup.value;
    const produto: ProdutoRequest = {
      nome: valores.nome.trim(),
      descricao: (valores.descricao ?? '').trim(),
      preco: Number(valores.preco),
      estoque: Number(valores.estoque),
      categoriaId: Number(valores.categoriaId),
    };
    if (this.imagemBase64) {
      produto.imagemBase64 = this.imagemBase64;
    } else if (this.removerImagem) {
      produto.removerImagem = true;
    }
    const request = valores.id
      ? this.produtoService.editar(valores.id, produto)
      : this.produtoService.cadastrar(produto);

    this.salvando = true;
    request.subscribe({
      next: () => this.router.navigate(['/produtos']),
      error: (erro) => {
        this.erro =
          erro.status === 413
            ? 'A imagem ultrapassou o tamanho permitido. Escolha uma imagem de até 5 MB.'
            : typeof erro.error?.mensagem === 'string'
              ? erro.error.mensagem
              : 'Não foi possível salvar o produto. Confira os dados e a imagem e tente novamente.';
        this.salvando = false;
        this.changeDetector.markForCheck();
      },
    });
  }

  excluir(): void {
    if (this.ocupado) return;
    const id = this.formGroup.get('id')?.value;
    if (id && confirm('Deseja excluir este produto?')) {
      this.erro = '';
      this.excluindo = true;
      this.produtoService.excluir(id).subscribe({
        next: () => this.router.navigate(['/produtos']),
        error: () => {
          this.erro = 'Não foi possível excluir o produto. Tente novamente.';
          this.excluindo = false;
          this.changeDetector.markForCheck();
        },
      });
    }
  }
}
