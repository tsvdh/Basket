; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

#define AppName "Basket"
#define AppVersion "0.1.0"
#define AppPublisher "Tobias van den Hurk"
#define AppExeLocation "{app}\image\bin\Basket.exe"
#define ProjectLocation "C:\Users\tsvdh\IntelliJProjects\Basket"
#define IconLocation "\src\main\resources\images\icon.ico"

[Setup]
; NOTE: The value of AppId uniquely identifies this application. Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{4DF64C69-92A3-4E25-8C94-5C22B27630C8}
AppName={#AppName}
AppVersion={#AppVersion}
;AppVerName={#AppName} {#AppVersion}
AppPublisher={#AppPublisher}
DefaultDirName={autoappdata}\{#AppName}
DisableDirPage=yes
DisableProgramGroupPage=yes
DisableWelcomePage=no
; Remove the following line to run in administrative install mode (install for all users.)
PrivilegesRequired=lowest
OutputBaseFilename={#AppName}-{#AppVersion}
Compression=lzma
SolidCompression=yes
WizardStyle=modern
SignTool=gpg --output $f --sign $f
SignedUninstaller=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "{#ProjectLocation}\program\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#ProjectLocation}\target\image\*"; DestDir: "{app}\image"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#ProjectLocation}\src\main\resources\style\*"; DestDir: "{app}\resources\style"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\{#AppName}"; Filename: "{#AppExeLocation}"; IconFilename: "{#ProjectLocation}\{#IconLocation}"
Name: "{autodesktop}\{#AppName}"; Filename: "{#AppExeLocation}"; Tasks: desktopicon; IconFilename: "{#ProjectLocation}\{#IconLocation}"

[Run]
Filename: "{#AppExeLocation}"; Description: "{cm:LaunchProgram,{#StringChange(AppName, '&', '&&')}}"; Flags: shellexec postinstall skipifsilent

