import { Component } from '@angular/core';
import {Router} from "@angular/router";
import {AuthenticationService} from "../../services/services/authentication.service";
import {NgIf} from "@angular/common";
import {CodeInputModule} from "angular-code-input";

@Component({
  selector: 'app-activate-account',
  standalone: true,
  imports: [
    NgIf,
    CodeInputModule
  ],
  templateUrl: './activate-account.component.html',
  styleUrl: './activate-account.component.scss'
})
export class ActivateAccountComponent {

    message = '';
    isOkay = true;
    submitted = false;
    constructor(
      private router: Router,
      private authService: AuthenticationService
    ) {}
  private confirmAccount(token: string) {
    this.authService.confirm({
      token
    }).subscribe( {
      next: () => {
        this.message = 'Your account has been successfully activated.\nNow you can proceed to Login';
        this.submitted = true;
      },
      error: (err) => {
        console.log(err.message);
        this.message = 'Token has been expired or invalid';
        this.submitted = true;
        this.isOkay = false;
      }
    });
  }

  onCodeCompleted(token: string) {
      console.log(token)
    this.confirmAccount(token)
  }

  redirectToLogin() {
    this.router.navigate(['login']);
  }
}
