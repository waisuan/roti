package controllers

class AuthControllerTest {

    // @After
    // fun clearMocks() {
    //     Mockito.framework().clearInlineMocks()
    // }
    //
    // @Test
    // fun `handles request if JWT token has been verified successfully`() {
    //     val handler = mock<Handler>()
    //     val context = mock<Context>()
    //     val token = Validator.generateToken()
    //
    //     whenever(context.header(any())).thenReturn("Bearer $token")
    //     whenever(context.matchedPath()).thenReturn("/some/path")
    //     whenever(context.method()).thenReturn("GET")
    //     whenever(context.status(any())).thenReturn(context)
    //     whenever(handler.handle(any())).doAnswer { Unit }
    //
    //     AuthController.accessManager(handler, context, HashSet<Role>())
    //
    //     verify(handler).handle(any())
    // }
    //
    // @Test
    // fun `discards request if JWT token is invalid`() {
    //     val handler = mock<Handler>()
    //     val context = mock<Context>()
    //
    //     whenever(context.header(any())).thenReturn("Bearer BAD_TOKEN")
    //     whenever(context.matchedPath()).thenReturn("/some/path")
    //     whenever(context.method()).thenReturn("GET")
    //     whenever(context.status(any())).thenReturn(context)
    //     whenever(handler.handle(any())).doAnswer { Unit }
    //
    //     AuthController.accessManager(handler, context, HashSet<Role>())
    //
    //     verify(context).status(any())
    // }
}
