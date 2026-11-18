# App Search — Android projekat

Jednostavna Android aplikacija za pretragu i pokretanje aplikacija koje imaju ikonicu u Android launcher-u.

## Funkcije
- Pretraga po celom nazivu ili delu naziva, dok kucaš.
- Dodir na rezultat pokreće izabranu aplikaciju.
- Radi lokalno, bez interneta i bez naloga.
- Minimalna verzija Androida: 6.0 (API 23); ciljna verzija: Android 15 / API 35; kompajliranje uz SDK 36.

## Pravljenje APK-a bez instalacije Android Studija na računaru

U projektu je dodat GitHub Actions postupak za izgradnju APK-a u oblaku.

1. Napravi GitHub nalog na https://github.com ako ga nemaš.
2. Napravi novi repository (može da bude Private).
3. Otpakuj ZIP na računaru.
4. Na GitHub stranici repozitorijuma izaberi **Add file > Upload files**.
5. Prevuci sadržaj otpakovanog foldera u prozor za upload, uključujući foldere `app` i `.github`. Ako ne vidiš `.github`, uključi prikaz skrivenih stavki u Windows File Explorer-u.
6. Klikni **Commit changes**.
7. Otvori karticu **Actions**, izaberi workflow **Build App Search APK**, zatim **Run workflow**.
8. Kada se završi bez greške (zelena oznaka), otvori taj run i preuzmi artifact **App-Search-APK**.
9. Raspakuj preuzeti artifact ZIP. U njemu se nalazi `app-debug.apk`.

## Napomena
Aplikacija prikazuje aplikacije koje Android izlaže kroz standardnu MAIN/LAUNCHER listu. Neke sistemske komponente bez ikone za pokretanje neće se pojaviti.
