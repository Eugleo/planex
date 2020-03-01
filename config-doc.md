## Dokumentace konfiguračního souboru
*Předpokládá se, že chápete, jak probíhá výpočet [důležitosti předmětu](README.md#důležitost-předmětu).*

Konfigurační soubor Planexu má koncovku `.plx`. Nachází se v něm všechny informace, které Planex potřebuje k chodu. Dělí se v zásadě na tři oddíly:

- obecná nastavení
- nastavení globálních atributů předmětů
- nastavení atributů jednotlivých předmětů

Ve všech oddílech se atributy specifikují jedním ze dvou způsobů:

```scheme
; vlastnosti s hodnotou
; hodnota může být číslo, řetězec, nebo datum
- jméno: hodnota

; vypínače
; když se někde objeví, daná vlastnost je "zapnutá"
; když někde není, je daná vlastnost "vypnutá"
- vypínač

; pokud chceme vypínač explicitně zapnout nebo vypnout
; můžeme jej použít jako vlastnost s hodnotou true/false
- vypínač: true
- vypínač: false
```

Nyní popíšeme vlastnosti a vypínače, které se v každém z oddílů mohou objevit. Pokud **je** nutné, aby vlastnost v souboru byla, je zde za jejím jménem vykřičník (v souboru však už není). V rámci jednotlivých oddílů na pořadí parametrů nezáleží.

###Obecná nastavení

```
- rok: [číslo]
```

Kdekoli se *po tomto atributu* objeví datum bez specifikovaného roku, jedná se o tento rok. Slouží pouze ke zkrácení zápisu.

```
- začátek!: [datum]
```

Udává, kdy se uživatel může začít učit (zkoušky před tímto datem se tedy ani neberou v úvahu).

```
- v: [číslo]
```

Udává hodnotu parametru u váhy. 

```
- k: [číslo]
```

Udává hodnotu parametru u kreditů.

```
- s: [číslo]
```

Udává hodnotu parametru u statusu.

```
- st: [číslo], [číslo], [číslo]
```

Udává hodnotu funkce $st$ pro povinné, povinně volitelné a volitelné předměty (respektive).

Obecná nastavení jsou od následujícího oddílu oddělena řádkem s třemi plusy.

### Nastavení globálních atributů předmětů

Mnoho atributů bude jistě u většiny předmětů stejných (váha, datum, kdy můžete nejdříve na zkoušku, atp.), proto je zbytečné tyto společné vlasnosti vypisovat u každého předmětu zvlášť. Tento oddíl slouží ke stanovení výchozích hodnot jednotlivých atributů, které pak mohou být selektivně u jednotlivých předmětů přepsány.

Pokud nějaký (volitelný) atribut chybí jak u daného předmětu, tak zde v globálním nastavení, použije se jeho interní výchozí hodnota, což většinou bývá něco neutrálního (0, 1, nebo v případě dat "bez omezení").

```
- váha: Udává váhu předmětu
```

Udává váhu předmětu.

```
- pokusy: [číslo]
```

Udává minimální počet zbývajících náhradních pokusů. Jedná se o pevnou hranici, takže se může stát, že zadání nebude mít řešení.

```
- rozmezí: [datum|x] - [datum|x]
```

Udává rozmezí, ve kterém je možné skládat zkoušky. Spodní hranici je možno využít, pokud například až do konce ledna určitě nebudete mít nutné zápočty, a horní, pokud ke konci zkouškového už plánujete lyžovat. Pokud se místo data vyskytne `x`, bere se to jako `bez omezení`.

### Nastavení atributů jednotlivých předmětů

Nastavení každého předmětu je uvozeno hlavičkou. Všechny atributy až do další hlavičky pak patří tomuto předmětu.

```
= Jméno předmětu (unikátní kód předmětu, nejlépe ze SISu)
```

Kód v závorkách musí být unikátní, s jedinou výjimkou: pokud chcete rozdílnou konfiguraci pro zkoušku a zápočet, je možné pro ně nastavit atributy zvlášť, přestože mají stejný kód.

Předmětu je možno nastavit všechny atributy, které je možno nastavit globálně (viz výše), plus nějaké další (viz níže). Pokud dojde ke střetu, atributy nastavené u předmětu přepisují ty nastavené globálně.

```
- termíny!: [datum], ..., [datum]
```

Udává dny, kdy je možno z daného předmětu konat zkoušku.

```
- kredity: [číslo]
```

Udává kreditové ohodnocení předmětu.

```
- status: [P|PVP|V]
```

Udává status předmětu.

```
- optimum: [číslo] [dní]?
```

Udává počet dní, které byste v ideálním případě chtěli mít na přípravu. Nejedná se o pevnou hranici, pouze o ideální stav.

```
- minimum: [číslo] [dní]?
```

Udává nejnižší počet dní, které potřebujete na zkoušku. Jedná se na roudíl od předchozícho atributu o pevnou hranici, takže se může stát, že zadání nebude mít řešení.

```
- ignorovat
```

Udává, zda by měl být předmět modelem ignorován. Může posloužit v krajních případech, kdy model není schopen najít uspokojující řešení.

```
- zápočet
```

Udává, zda je předmět zápočet. Pokud se vyskytnou dva předměty se stejným identifikačním číslem, ale jeden bude zápočet a druhý ne, budou modelem vnímány jako dva různé předměty.

Pro úplnost dodejme, že i atributy z tohoto oddílu jdou nastavit globálně, ale nedává to příliš smysl.