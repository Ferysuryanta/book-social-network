import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import {HTTP_INTERCEPTORS, HttpClient, provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import { LoginComponent } from './pages/login/login.component';
import {RegisterComponent} from "./pages/register/register.component";
import {ActivateAccountComponent} from "./pages/activate-account/activate-account.component";
import {httpTokenInterceptor} from "./services/interceptor/http-token.interceptor";
import {TokenService} from "./services/token/token.service";
import {AuthenticationService} from "./services/services/authentication.service";

export let appConfig: ApplicationConfig;
appConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    LoginComponent,
    RegisterComponent,
    ActivateAccountComponent,
    HttpClient,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: httpTokenInterceptor,
      multi: true
    }
  ]
};
