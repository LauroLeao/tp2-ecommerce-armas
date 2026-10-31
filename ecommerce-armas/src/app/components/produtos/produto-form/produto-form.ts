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
import { MarcaService } from '../../../services/marca.service';
import { ModeloService } from '../../../services/modelo.service';
import { FornecedorService } from '../../../services/fornecedor.service';
import { Marca } from '../../../models/marca.model';
import { Modelo } from '../../../models/modelo.model';
import { Fornecedor } from '../../../models/fornecedor.model';
import { forkJoin, Subscription } from 'rxjs';

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
  marcas: Marca[] = [];
  modelos: Modelo[] = [];
  fornecedores: Fornecedor[] = [];
  erroVinculos = '';
  erro = '';
  salvando = false;
  excluindo = false;
  carregandoCategorias = true;
  carregandoVinculos = false;
  private alteracaoMarca?: Subscription;
  private consultaVinculos?: Subscription;
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
    private marcaService: MarcaService,
    private modeloService: ModeloService,
    private fornecedorService: FornecedorService,
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

  get marcaPreview(): string {
    const id = Number(this.formGroup?.get('marcaId')?.value);
    return this.marcas.find((marca) => marca.id === id)?.nome || 'Selecione uma marca';
  }

  get modeloPreview(): string {
    const id = Number(this.formGroup?.get('modeloId')?.value);
    return this.modelos.find((modelo) => modelo.id === id)?.nome || 'Selecione um modelo';
  }

  get fornecedorPreview(): string {
    const id = Number(this.formGroup?.get('fornecedorId')?.value);
    return this.fornecedores.find((fornecedor) => fornecedor.id === id)?.nome || 'Não informado';
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
      marcaId: [produto?.marcaId ?? null, Validators.required],
      modeloId: [produto?.modeloId ?? null, Validators.required],
      fornecedorId: [produto?.fornecedorId ?? null],
    });

    this.marcaService.listar().subscribe({
      next: (marcas) => {
        this.marcas = marcas;
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.erroVinculos = 'Não foi possível carregar as marcas. Verifique o servidor.';
        this.changeDetector.markForCheck();
      },
    });
    this.alteracaoMarca = this.formGroup.get('marcaId')?.valueChanges.subscribe((id) => {
      this.formGroup.patchValue({ modeloId: null, fornecedorId: null }, { emitEvent: false });
      this.carregarVinculos(id);
    });
    if (produto?.marcaId) this.carregarVinculos(produto.marcaId);

    this.categoriaService.listar().subscribe({
      next: (categorias) => {
        this.categorias = categorias;
        this.carregandoCategorias = false;
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.carregandoCategorias = false;
        this.erro =
          'Não foi possível carregar as categorias. Verifique se o servidor está em execução e atualize a página.';
        this.changeDetector.markForCheck();
      },
    });
  }

  ngOnDestroy(): void {
    this.imagemOperacao++;
    this.alteracaoMarca?.unsubscribe();
    this.consultaVinculos?.unsubscribe();
  }

  private carregarVinculos(marcaId: number | null): void {
    this.consultaVinculos?.unsubscribe();
    this.modelos = [];
    this.fornecedores = [];
    this.erroVinculos = '';
    this.carregandoVinculos = !!marcaId;
    if (!marcaId) return;
    this.consultaVinculos = forkJoin({
      modelos: this.modeloService.listar(Number(marcaId)),
      fornecedores: this.fornecedorService.listar(Number(marcaId)),
    }).subscribe({
      next: ({ modelos, fornecedores }) => {
        this.modelos = modelos;
        this.fornecedores = fornecedores;
        this.carregandoVinculos = false;
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.erroVinculos = 'Não foi possível carregar modelos e fornecedores desta marca.';
        this.carregandoVinculos = false;
        this.changeDetector.markForCheck();
      },
    });
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
      marcaId: null,
      modeloId: null,
      fornecedorId: null,
    });
    this.erro = '';
    this.retirarImagem();
  }

  salvar(): void {
    if (this.ocupado || this.carregandoImagem() || this.carregandoVinculos) return;
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
      modeloId: Number(valores.modeloId),
      fornecedorId: valores.fornecedorId ? Number(valores.fornecedorId) : null,
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
        error: (resposta) => {
          this.erro = resposta.error?.mensagem ?? 'Não foi possível excluir o produto. Tente novamente.';
          this.excluindo = false;
          this.changeDetector.markForCheck();
        },
      });
    }
  }
}
