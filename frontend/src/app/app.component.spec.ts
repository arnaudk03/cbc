import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideHttpClient(), provideRouter([])]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should expose the SONAGESS application title', () => {
    const fixture = TestBed.createComponent(AppComponent);
    expect(fixture.componentInstance.titre).toContain('SONAGESS');
  });

  it('should hide the authenticated shell for anonymous visitors', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.afficherCoquille()).toBeFalse();
    const toolbar = fixture.nativeElement.querySelector('mat-toolbar');
    expect(toolbar).toBeNull();
  });

  const menusParRole: Record<string, string[]> = {
    ADMIN: ['/dashboard', '/collectes', '/paiements', '/stocks', '/referentiels'],
    DCT: ['/dashboard', '/collectes', '/stocks', '/referentiels'],
    SCP: ['/dashboard', '/collectes', '/referentiels'],
    DFC_DIRECTEUR: ['/dashboard', '/paiements', '/referentiels'],
    SFB_INITIATEUR: ['/dashboard', '/paiements', '/referentiels'],
    SFB_CHEF: ['/dashboard', '/paiements'],
    COMPTABILITE: ['/dashboard', '/referentiels'],
    DGSA: ['/dashboard', '/stocks', '/referentiels'],
    SGS: ['/dashboard', '/stocks', '/referentiels'],
    MAGASINIER: ['/dashboard', '/stocks', '/referentiels'],
    AGENT_COLLECTEUR: ['/dashboard', '/collectes', '/stocks', '/referentiels'],
    CHEF_COMPTOIR: ['/dashboard', '/collectes', '/stocks', '/referentiels'],
    DPL: ['/dashboard', '/stocks', '/referentiels'],
    CONSULTATION: ['/dashboard', '/referentiels']
  };

  for (const [role, routes] of Object.entries(menusParRole)) {
    it(`should only show relevant menus for ${role}`, () => {
      localStorage.setItem('cbc.accessToken', 'test-token');
      localStorage.setItem(
        'cbc.user',
        JSON.stringify({
          id: 1,
          matricule: 'TEST001',
          nom: 'Test',
          prenom: 'Role',
          email: 'test@sonagess.bf',
          telephone: '00000000',
          roles: [role],
          permissions: []
        })
      );

      const fixture = TestBed.createComponent(AppComponent);
      fixture.detectChanges();

      expect(fixture.componentInstance.menusVisibles().map((menu) => menu.route)).toEqual(routes);
    });
  }
});
