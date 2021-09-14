; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

#define AppName "Basket"
#define AppVersion "0.1.0"
#define AppPublisher "Tobias van den Hurk"
#define AppExeName "Basket.bat"
#define AppExeLocation "{app}\image\bin\{#MyAppExeName}"
#define ProjectLocation "C:\Users\tsvdh\IntelliJProjects\Basket"
#define IconLocation "{#ProjectLocation}\src\main\resources\images\icon.ico"

[Setup]
; NOTE: The value of AppId uniquely identifies this application. Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{4DF64C69-92A3-4E25-8C94-5C22B27630C8}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
;AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\{#MyAppName}
DisableProgramGroupPage=yes
; Remove the following line to run in administrative install mode (install for all users.)
PrivilegesRequired=lowest
OutputBaseFilename={#MyAppName}-{#MyAppVersion}
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Dirs]
Name: "{app}\image"
Name: "{app}\library"
Name: "{app}\resources"
Name: "{app}\resources\private"
Name: "{app}\resources\public"
Name: "{app}\resources\public\style"
Name: "{userappdata}\{#MyAppName}"

[Files]
Source: "{#ProjectLocation}\target\image\*"; DestDir: "{app}\image"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#ProjectLocation}\src\main\resources\style\*"; DestDir: "{app}\resources\style"; Flags: ignoreversion
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{#MyAppExeLocation}"; IconFilename: "{#IconLocation}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{#MyAppExeLocation}"; Tasks: desktopicon; IconFilename: "{#IconLocation}

[Run]
Filename: "{#MyAppExeLocation}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: shellexec postinstall skipifsilent

