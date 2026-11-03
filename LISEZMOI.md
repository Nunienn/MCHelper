# MCHelper, version adaptée Shield + Yamaha R-N800A

Basé sur https://github.com/ellamosi/MCHelper (licence d'origine conservée).

## Ce que fait cette version
- **Réveil de la Shield** → allume l'ampli, attend qu'il soit prêt, choisit l'entrée, puis **baisse le volume s'il dépasse le plafond** (jamais il ne le monte).
- **Mise en veille de la Shield** → met l'ampli en veille, seulement s'il est sur l'entrée de la Shield (une écoute de musique en cours n'est pas coupée).
- **Démarrage à froid de la Shield** → même chose que le réveil.
- Supprimé : la recopie du volume Android TV vers l'ampli (risque de volume fort).
- Ajouté : délais d'attente, 3 tentatives par commande, et une erreur n'arrête plus le service.
- Réglages modifiables sans recompiler (par ADB, voir étape 5).

## 1. Préparer l'ampli
- Menu Setup > Network > **Network Standby = On** (pas « Auto » : en « Auto », l'ampli peut passer dans un mode d'économie d'énergie où il ne répond plus au réseau).
- Dans l'interface de votre box : notez l'**IP locale** de l'ampli (du type 192.168.x.x) et réservez-la (bail DHCP fixe).

## 2. Trouver le nom de l'entrée
1. Mettez l'ampli manuellement sur l'entrée où arrive le son du XGIMI.
2. Dans un navigateur, sur votre réseau : `http://IP_AMPLI/YamahaExtendedControl/v1/main/getStatus`
3. Relevez la valeur de `"input"` (ex. `optical1`). La valeur `"max_volume"`, si elle est présente, est le volume max de l'ampli.

## 3. Compiler l'APK
- Android Studio → Open → ce dossier → Build > Build Bundle(s) / APK(s) > Build APK(s).
- Le projet utilise Android Gradle Plugin 7.4 : si Android Studio le demande, choisissez un JDK 11 ou 17 (Settings > Build Tools > Gradle > Gradle JDK).
- Fichier obtenu : `app/build/outputs/apk/debug/app-debug.apk`

## 4. Préparer la Shield
- Paramètres > Préférences de l'appareil > À propos > cliquez 7 fois sur « Build » (les options pour les développeurs apparaissent).
- Options pour les développeurs > **Débogage réseau** = activé. Notez l'IP de la Shield.
- Sur le PC : installez Android « platform-tools » (contient `adb`).

## 5. Installer et configurer
```
adb connect IP_SHIELD:5555        (acceptez l'autorisation sur l'écran)
adb install app-debug.apk
adb shell am start-foreground-service -n io.github.ellamosi.mchelper/.MCHService --es ip 192.168.1.50 --es input optical1 --ei maxStartVolumePct 30 --ez autoOff true
```
Remplacez `192.168.1.50` et `optical1` par vos valeurs.

| Réglage | Rôle | Défaut |
|---|---|---|
| `ip` | IP locale de l'ampli (refusée si ce n'est pas une IP privée valide) | aucune |
| `input` | Entrée sélectionnée à l'allumage | optical1 |
| `maxStartVolumePct` | Plafond du volume à l'allumage, en % du volume max de l'ampli (0 = désactivé) | 30 |
| `autoOff` | Veille de l'ampli avec la Shield | true |

Pour changer un réglage plus tard : relancez la commande `start-foreground-service` avec seulement l'extra à modifier.

## 6. Tester / dépanner
- Mettez la Shield en veille, puis réveillez-la : l'ampli doit s'allumer en quelques secondes.
- Journaux : `adb logcat -s MchService MCApi PowerController VolumeController MCHWorker Config`

## Réglage du plafond de volume
Sur les amplis Yamaha, le volume avance par pas de 0,5 dB : 30 % du max correspond à un volume faible.
Augmentez par paliers de 5 % jusqu'au niveau voulu.
