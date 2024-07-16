import { Component } from '@angular/core';
import { AuthenticationRequest } from '../../services/models';
import {FormsModule} from "@angular/forms";
import {NgFor, NgIf} from "@angular/common";
import {Router} from "@angular/router";
import {AuthenticationService} from "../../services/services/authentication.service";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    NgIf,
    NgFor,
    FormsModule

  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {

  constructor(
    private router: Router,
    private authService: AuthenticationService
    // another service
  ) {
  }
  authRequest: AuthenticationRequest = {email: '', password: ''};
  errorMsg: Array<string> = [];

  login() {
      this.errorMsg = [];
      this.authService.authenticate({
        body: this.authRequest
      }).subscribe({
        next: (res) => {
          // save the token
          this.router.navigate(['books']);
        },
        error:(err) => {
          console.log(err);
        }
      });
  }

  register() {
    this.router.navigate(['register'])
  }
}
