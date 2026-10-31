import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Observable } from 'rxjs';
import { Marca } from '../../models/marca.model';
import { MarcaService } from '../../services/marca.service';
import { ModeloService } from '../../services/modelo.service';
import { FornecedorService } from '../../services/fornecedor.service';
import { CategoriaService } from '../../services/categoria.service';
import { Modelo } from '../../models/modelo.model';

type TipoCadastro = 'categorias' | 'marcas' | 'modelos' | 'fornecedores';
interface Registro {
  id: number;
  nome: string;
  marcaId?: number;
  marcaNome?: string;
  email?: string;
  telefone?: string;
}

@Component({
  selector: 'app-cadastro-admin',
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatIconModule],
  templateUrl: './cadastro-admin.html',
  styleUrl: './cadastro-admin.css',
})
export class CadastroAdmin implements OnInit {
  readonly tipo: TipoCadastro;
  readonly singular: string;
  readonly titulo: string;
  readonly limiteNome: number;
  readonly changeDetector = inject(ChangeDetectorRef);
  formGroup!: FormGroup;
  registros: Registro[] = [];
  marcas: Marca[] = [];
  busca = '';
  carregando = true;
  salvando = false;
  excluindoId: number | null = null;
  erro = '';
  aviso = '';
  marcaEmFoco: Marca | null = null;
  modelosDaMarca: Modelo[] = [];
  modeloForm!: FormGroup;
  carregandoModelos = false;
  salvandoModelo = false;
  excluindoModeloId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private marcaService: MarcaService,
    private modeloService: ModeloService,
    private fornecedorService: FornecedorService,
    private categoriaService: CategoriaService,
  ) {
    this.tipo = this.route.snapshot.data['tipo'] as TipoCadastro;
    const nomes = {
      categorias: ['categoria', 'Categorias'],
      marcas: ['marca', 'Marcas'],
      modelos: ['modelo', 'Modelos'],
      fornecedores: ['fornecedor', 'Fornecedores'],
    };
    [this.singular, this.titulo] = nomes[this.tipo];
    this.limiteNome = this.tipo === 'categorias' || this.tipo === 'marcas' ? 80 : this.tipo === 'modelos' ? 100 : 120;
  }

  get editando(): boolean { return !!this.formGroup?.get('id')?.value; }
  get filtrados(): Registro[] {
    const busca = this.busca.trim().toLocaleLowerCase('pt-BR');
    return busca ? this.registros.filter((item) =>
      `${item.nome} ${item.marcaNome ?? ''} ${item.email ?? ''}`.toLocaleLowerCase('pt-BR').includes(busca))
      : this.registros;
  }

  ngOnInit(): void {
    this.formGroup = this.formBuilder.group({
      id: [null as number | null],
      nome: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(this.limiteNome)]],
      marcaId: [null as number | null, this.tipo === 'marcas' || this.tipo === 'categorias' ? [] : [Validators.required]],
      email: ['', this.tipo === 'fornecedores' ? [Validators.required, Validators.email, Validators.maxLength(160)] : []],
      telefone: ['', this.tipo === 'fornecedores' ? [Validators.required, Validators.pattern(/^\d{10,11}$/)] : []],
    });
    this.modeloForm = this.formBuilder.group({
      id: [null as number | null],
      nome: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
    });
    this.carregar();
    if (this.tipo === 'modelos' || this.tipo === 'fornecedores') this.carregarMarcas();
  }

  buscar(valor: string): void { this.busca = valor; }

  novo(): void {
    this.formGroup.reset({ id: null, nome: '', marcaId: null, email: '', telefone: '' });
    this.erro = '';
    this.aviso = '';
  }

  selecionar(item: Registro): void {
    this.formGroup.reset({
      id: item.id, nome: item.nome, marcaId: item.marcaId ?? null,
      email: item.email ?? '', telefone: item.telefone ?? '',
    });
    this.erro = '';
    this.aviso = '';
    if (this.tipo === 'marcas') this.focarMarca(item as Marca);
  }

  focarMarca(marca: Marca): void {
    this.marcaEmFoco = marca;
    this.novoModelo();
    this.carregarModelos();
  }

  novoModelo(): void {
    this.modeloForm.reset({ id: null, nome: '' });
  }

  selecionarModelo(modelo: Modelo): void {
    this.modeloForm.reset({ id: modelo.id, nome: modelo.nome });
  }

  carregarModelos(): void {
    if (!this.marcaEmFoco) return;
    const marcaId = this.marcaEmFoco.id;
    this.carregandoModelos = true;
    this.modeloService.listar(marcaId).subscribe({
      next: (modelos) => {
        if (this.marcaEmFoco?.id !== marcaId) return;
        this.modelosDaMarca = modelos;
        this.carregandoModelos = false;
        this.changeDetector.markForCheck();
      },
      error: () => {
        if (this.marcaEmFoco?.id !== marcaId) return;
        this.erro = 'Não foi possível carregar os modelos desta marca.';
        this.carregandoModelos = false;
        this.changeDetector.markForCheck();
      },
    });
  }

  salvarModelo(): void {
    if (!this.marcaEmFoco || this.salvandoModelo || this.modeloForm.invalid) {
      this.modeloForm.markAllAsTouched();
      return;
    }
    const { id, nome } = this.modeloForm.getRawValue();
    const dados = { nome: String(nome).trim(), marcaId: this.marcaEmFoco.id };
    this.salvandoModelo = true;
    this.erro = '';
    const pedido = id ? this.modeloService.editar(id, dados) : this.modeloService.cadastrar(dados);
    pedido.subscribe({
      next: () => {
        this.salvandoModelo = false;
        this.novoModelo();
        this.aviso = 'Modelo salvo com sucesso.';
        this.carregarModelos();
      },
      error: (resposta) => {
        this.salvandoModelo = false;
        this.erro = resposta.error?.mensagem ?? 'Não foi possível salvar o modelo.';
        this.changeDetector.markForCheck();
      },
    });
  }

  excluirModelo(modelo: Modelo): void {
    if (this.excluindoModeloId !== null || !confirm(`Excluir modelo "${modelo.nome}"?`)) return;
    this.excluindoModeloId = modelo.id;
    this.erro = '';
    this.modeloService.excluir(modelo.id).subscribe({
      next: () => {
        this.excluindoModeloId = null;
        if (this.modeloForm.get('id')?.value === modelo.id) this.novoModelo();
        this.carregarModelos();
      },
      error: (resposta) => {
        this.excluindoModeloId = null;
        this.erro = resposta.error?.mensagem ?? 'Não foi possível excluir o modelo.';
        this.changeDetector.markForCheck();
      },
    });
  }

  somenteDigitos(): void {
    const control = this.formGroup.get('telefone');
    const valor = String(control?.value ?? '');
    const digitos = valor.replace(/\D/g, '').slice(0, 11);
    if (valor !== digitos) control?.setValue(digitos);
  }

  carregar(): void {
    this.carregando = true;
    this.erro = '';
    this.listar().subscribe({
      next: (registros) => {
        this.registros = registros;
        if (this.tipo === 'marcas' && this.marcaEmFoco) {
          this.marcaEmFoco = registros.find((item) => item.id === this.marcaEmFoco?.id) as Marca ?? null;
          if (!this.marcaEmFoco) this.modelosDaMarca = [];
        }
        this.carregando = false;
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.carregando = false;
        this.erro = `Não foi possível carregar ${this.titulo.toLowerCase()}. Verifique o back-end.`;
        this.changeDetector.markForCheck();
      },
    });
  }

  private carregarMarcas(): void {
    this.marcaService.listar().subscribe({
      next: (marcas) => { this.marcas = marcas; this.changeDetector.markForCheck(); },
      error: () => {
        this.erro = 'Não foi possível carregar as marcas. Verifique o back-end.';
        this.changeDetector.markForCheck();
      },
    });
  }

  salvar(): void {
    if (this.salvando || this.excluindoId !== null) return;
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      return;
    }
    const dados = this.formGroup.getRawValue();
    const nome = String(dados.nome).trim();
    const marcaId = Number(dados.marcaId);
    const email = String(dados.email).trim().toLowerCase();
    const telefone = String(dados.telefone).trim();
    const id = dados.id as number | null;
    let pedido: Observable<Registro>;
    if (this.tipo === 'categorias') {
      pedido = id ? this.categoriaService.editar(id, nome) : this.categoriaService.cadastrar(nome);
    } else if (this.tipo === 'marcas') {
      pedido = id ? this.marcaService.editar(id, { nome }) : this.marcaService.cadastrar({ nome });
    } else if (this.tipo === 'modelos') {
      pedido = id ? this.modeloService.editar(id, { nome, marcaId }) : this.modeloService.cadastrar({ nome, marcaId });
    } else {
      pedido = id
        ? this.fornecedorService.editar(id, { nome, marcaId, email, telefone })
        : this.fornecedorService.cadastrar({ nome, marcaId, email, telefone });
    }
    this.salvando = true;
    this.erro = '';
    this.aviso = '';
    pedido.subscribe({
      next: (registro) => {
        this.salvando = false;
        this.novo();
        this.aviso = `${this.singular[0].toUpperCase() + this.singular.slice(1)} salvo com sucesso.`;
        if (this.tipo === 'marcas') this.focarMarca(registro as Marca);
        this.carregar();
      },
      error: (resposta) => {
        this.salvando = false;
        this.erro = resposta.error?.mensagem ?? resposta.error?.errors?.[0]?.message
          ?? `Não foi possível salvar o ${this.singular}.`;
        this.changeDetector.markForCheck();
      },
    });
  }

  excluir(item: Registro): void {
    if (this.excluindoId !== null || !confirm(`Excluir ${this.singular} "${item.nome}"?`)) return;
    this.excluindoId = item.id;
    this.erro = '';
    const pedido = this.tipo === 'categorias' ? this.categoriaService.excluir(item.id)
      : this.tipo === 'marcas' ? this.marcaService.excluir(item.id)
      : this.tipo === 'modelos' ? this.modeloService.excluir(item.id)
      : this.fornecedorService.excluir(item.id);
    pedido.subscribe({
      next: () => {
        this.excluindoId = null;
        if (this.formGroup.get('id')?.value === item.id) this.novo();
        this.carregar();
      },
      error: (resposta) => {
        this.excluindoId = null;
        this.erro = resposta.error?.mensagem ?? `Não foi possível excluir o ${this.singular}.`;
        this.changeDetector.markForCheck();
      },
    });
  }

  private listar(): Observable<Registro[]> {
    return this.tipo === 'categorias' ? this.categoriaService.listar()
      : this.tipo === 'marcas' ? this.marcaService.listar()
      : this.tipo === 'modelos' ? this.modeloService.listar()
      : this.fornecedorService.listar();
  }
}
