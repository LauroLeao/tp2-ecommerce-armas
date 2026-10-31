import { FormControl } from '@angular/forms';
import { cpfValidator } from './cpf.validator';

describe('cpfValidator', () => {
  it('aceita os digitos verificadores corretos', () => {
    expect(cpfValidator(new FormControl('12345678909'))).toBeNull();
  });

  it('rejeita CPF de 11 digitos com verificadores incorretos ou repetidos', () => {
    expect(cpfValidator(new FormControl('12345678900'))).toEqual({ cpf: true });
    expect(cpfValidator(new FormControl('11111111111'))).toEqual({ cpf: true });
  });
});
