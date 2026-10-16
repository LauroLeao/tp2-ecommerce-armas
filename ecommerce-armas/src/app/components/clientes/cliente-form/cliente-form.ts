import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ClienteService } from '../../../services/cliente.service';
import { Cliente } from '../../../models/cliente.model';

@Component({
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
  ],
  selector: 'app-cliente-form',
  styleUrl: './cliente-form.css',
  templateUrl: './cliente-form.html',
})
export class ClienteForm implements OnInit {
  formGroup!: FormGroup;
  salvando = false;
  excluindo = false;
  erro = '';
  private readonly changeDetector = inject(ChangeDetectorRef);

  get editando(): boolean {
    return Boolean(this.formGroup?.get('id')?.value);
  }

  get iniciais(): string {
    const nome = String(this.formGroup?.get('nome')?.value ?? '');
    const partes = nome.trim().split(/\s+/).filter(Boolean);
    return partes.length
      ? `${partes[0][0]}${partes.length > 1 ? partes[partes.length - 1][0] : ''}`.toUpperCase()
      : '?';
  }

  constructor(
    private formBuilder: FormBuilder,
    private clienteService: ClienteService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    const cliente: Cliente = this.activatedRoute.snapshot.data['cliente'];

    this.formGroup = this.formBuilder.group({
      id: [cliente?.id ?? null],
      nome: [cliente?.nome ?? '', Validators.required],
      email: [cliente?.email ?? '', [Validators.required, Validators.email]],
      telefone: [cliente?.telefone ?? '', Validators.required],
      endereco: [cliente?.endereco ?? '', Validators.required],
    });
  }

  salvar(): void {
    if (this.salvando || this.excluindo) {
      return;
    }
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      return;
    }

    const cliente = this.formGroup.value as Cliente;
    const request = cliente.id
      ? this.clienteService.editar(cliente.id, cliente)
      : this.clienteService.cadastrar(cliente);

    this.salvando = true;
    this.erro = '';
    request.subscribe({
      next: () => this.router.navigate(['/clientes']),
      error: () => {
        this.salvando = false;
        this.erro = 'Não foi possível salvar o cliente. Confira os dados e tente novamente.';
        this.changeDetector.markForCheck();
      },
    });
  }

  excluir(): void {
    const id = this.formGroup.value.id;
    if (id && !this.salvando && !this.excluindo && confirm('Deseja excluir este cliente?')) {
      this.excluindo = true;
      this.erro = '';
      this.clienteService.excluir(id).subscribe({
        next: () => this.router.navigate(['/clientes']),
        error: () => {
          this.excluindo = false;
          this.erro = 'Não foi possível excluir o cliente. Tente novamente.';
          this.changeDetector.markForCheck();
        },
      });
    }
  }
}
