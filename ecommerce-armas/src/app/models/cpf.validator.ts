import { AbstractControl, ValidationErrors } from '@angular/forms';

export function cpfValidator(control: AbstractControl): ValidationErrors | null {
  const cpf = String(control.value ?? '');
  if (!cpf) return null;
  if (!/^\d{11}$/.test(cpf) || /^(\d)\1{10}$/.test(cpf)) return { cpf: true };

  for (let tamanho = 9; tamanho <= 10; tamanho++) {
    let soma = 0;
    for (let i = 0; i < tamanho; i++) {
      soma += Number(cpf[i]) * (tamanho + 1 - i);
    }
    const digito = (soma * 10) % 11;
    if ((digito === 10 ? 0 : digito) !== Number(cpf[tamanho])) return { cpf: true };
  }
  return null;
}
