# 🚀 EyeDock - Plano de Implementação Completa

## 📋 **Objetivos**
Implementar todas as funcionalidades previstas nas instruções originais:
- ONVIF/RTSP discovery e streaming  
- QR Code scanner para adicionar câmeras
- Interface completa com PTZ controls
- Storage Access Framework (SAF)
- Recording e playback
- Motion/sound events
- Two-way audio

## 🎯 **Fases de Implementação**

### **Fase 1: Reabilitar Módulos Core ✅**
- [x] core:common (já funcionando)
- [ ] core:onvif (reabilitar com correções)
- [ ] core:media (reabilitar com correções) 
- [ ] core:storage (reabilitar com correções)
- [ ] core:events (reabilitar com correções)
- [ ] core:ui (reabilitar com correções)

### **Fase 2: UI Components Principais**
- [ ] QR Scanner com ML Kit
- [ ] Add Camera Form (manual)
- [ ] Camera Wall/Grid 
- [ ] Live View Player
- [ ] PTZ Controls (joystick)
- [ ] Timeline Playback

### **Fase 3: Storage & Recording**
- [ ] Storage Access Framework (SAF) picker
- [ ] Recording service
- [ ] Metadata management
- [ ] Retention policies

### **Fase 4: ONVIF & Streaming**
- [ ] ONVIF device discovery
- [ ] RTSP connection manager
- [ ] Stream profiles detection
- [ ] PTZ capabilities

### **Fase 5: Events & Audio**
- [ ] Motion/sound detection
- [ ] Event notifications
- [ ] Two-way audio (talkback)
- [ ] Auto-tracking

### **Fase 6: Polish & Testing**
- [ ] Navigation complete
- [ ] Error handling
- [ ] Accessibility
- [ ] Performance optimization

## 🔧 **Ordem de Implementação**

1. **Reabilitar Hilt & DI** - Para dependency injection
2. **UI Core Components** - Base para interface
3. **QR Scanner** - Para adicionar câmeras facilmente  
4. **Storage SAF** - Para gravações
5. **ONVIF Discovery** - Para encontrar câmeras
6. **RTSP Streaming** - Para live view
7. **PTZ Controls** - Para controle de câmera
8. **Recording** - Para gravar streams
9. **Events** - Para motion/sound detection
10. **Polish** - Refinamentos finais

## 📱 **Funcionalidades por Tela**

### **Main/Cameras**
- Camera wall/grid (2x2, 3x3)
- Live thumbnails
- Status indicators
- FAB para adicionar câmera

### **Add Camera**  
- QR Scanner (ML Kit)
- Manual form
- Stream preview
- Test connection

### **Live View**
- RTSP/WebRTC player
- PTZ joystick
- Auto-tracking toggle
- Night vision toggle
- Record button
- Talkback (hold-to-talk)
- Snapshot button

### **Timeline**
- 24h ruler
- Seek controls
- Speed controls
- Export/share

### **Alerts**
- Motion events list
- Sound events list
- Jump to recording

### **Library**
- Recordings grid
- Search/filter
- Export/share
- Delete

### **Settings**
- Storage SAF picker
- Retention policies
- Privacy settings
- About/diagnostics

## ⚡ **Começar Agora**

Vou implementar na seguinte ordem:
1. Reabilitar Hilt no app
2. Corrigir módulos core (onvif, media, storage, events, ui)
3. Implementar QR Scanner 
4. Implementar Storage SAF
5. Implementar Camera Wall
6. Implementar Live View básico
7. Expandir funcionalidades progressivamente

**Objetivo: Produto Play Store ready com todas as funcionalidades!**
