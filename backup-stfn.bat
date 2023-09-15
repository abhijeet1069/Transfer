@echo off

rem Define source folder
set "src_code_folder=C:\Cisco\CVP\VXMLServer\applications\IDFC_SINGLE_TFN_CVP"
set "src_config_folder=C:\Servion\framework\IDFC_SINGLE_TFN_CVP"

rem Destination folder
set "destination_folder=C:\Servion_Backup\14_Sept_23_STFN_Max_tries_disc"

rem exclude log subfolder
set "exclude_code_subfolders=logs"
set "exclude_config_subfolders=logs"

rem Build the list of exclusions
set "code_exclusions="
for %%i in (%exclude_code_subfolders%) do (
  set "code_exclusions= /XD %src_code_folder%\%%i"
)

set "config_exclusions="
for %%i in (%exclude_config_subfolders%) do (
  set "config_exclusions= /XD %src_config_folder%\%%i"
)

robocopy "%src_code_folder%" "%destination_folder%\code" /E %code_exclusions%

robocopy "%src_config_folder%" "%destination_folder%\config" /E %config_exclusions%

pause